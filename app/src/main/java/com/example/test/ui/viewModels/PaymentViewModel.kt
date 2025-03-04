package com.example.test.ui.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.IOException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import com.example.test.BuildConfig
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val category: String = "",
    val amount: Long = 0L,
    val status: String = "PENDING",
    val paymentUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)


class PaymentViewModel : ViewModel() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()

    @RequiresApi(Build.VERSION_CODES.O)
    fun createTransaction(
        userId: String,
        amount: Int,
        paymentMethod: String,
        relatedId: String,
        relatedType: String,
        bankCode: String = "MANDIRI",
        ewalletChannel: String = "ID_OVO",
        retailOutlet: String = "ALFAMART",
        mobileNumber: String? = null,
        onSuccess: (String, Map<String, Any>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (amount <= 0) {
            onError("Amount must be greater than 0")
            return
        }

        val transactionId = "${relatedType.uppercase()}_${System.currentTimeMillis()}"
        val requestBody = JSONObject()
        var apiUrl = ""

        try {
            when (paymentMethod.uppercase()) {
                "VA" -> {
                    apiUrl = "https://api.xendit.co/callback_virtual_accounts"
                    requestBody.apply {
                        put("external_id", transactionId)
                        put("bank_code", bankCode.uppercase())
                        put("name", "User $userId")
                        put("expected_amount", amount) // Wajib untuk VA
                        put("is_single_use", true)
                        put("is_closed", true)
                        put("expiration_date", getExpiryTime())
                        put("metadata", JSONObject().apply {
                            put("related_id", relatedId)
                            put("related_type", relatedType)
                        })
                    }
                }

                "QRIS" -> {
                    apiUrl = "https://api.xendit.co/qr_codes"
                    requestBody.apply {
                        put("reference_id", transactionId)
                        put("type", "DYNAMIC")
                        put("currency", "IDR")
                        put("amount", amount)
                        put("expires_at", getExpiryTime())
                        put("metadata", JSONObject().apply {
                            put("related_id", relatedId)
                            put("related_type", relatedType)
                        })
                    }
                }

                "EWALLET" -> {
                    if (ewalletChannel.uppercase() == "ID_OVO" && mobileNumber.isNullOrEmpty()) {
                        onError("Mobile number is required for OVO")
                        return
                    }
                    apiUrl = "https://api.xendit.co/ewallets/charges"
                    requestBody.apply {
                        put("reference_id", transactionId)
                        put("currency", "IDR")
                        put("amount", amount)
                        put("checkout_method", "ONE_TIME_PAYMENT")
                        put("channel_code", ewalletChannel.uppercase())
                        put("channel_properties", JSONObject().apply {
                            put("success_redirect_url", "yourapp://success")
                            if (mobileNumber != null && ewalletChannel.uppercase() == "ID_OVO") {
                                put("mobile_number", mobileNumber)
                            }
                            when (ewalletChannel.uppercase()) {
                                "ID_DANA", "ID_SHOPEEPAY" -> {
                                    put("failure_redirect_url", "yourapp://failure")
                                }
                            }
                        })
                        // Tambahkan callback_url untuk eWallet
                        put(
                            "callback_url",
                            "https://xenditwebhook-4utu2r7iya-uc.a.run.app"
                        )
                        put("metadata", JSONObject().apply {
                            put("related_id", relatedId)
                            put("related_type", relatedType)
                        })
                    }
                }

                "RETAIL" -> {
                    apiUrl = "https://api.xendit.co/fixed_payment_code"
                    requestBody.apply {
                        put("external_id", transactionId)
                        put("retail_outlet_name", retailOutlet.uppercase())
                        put("name", "User $userId")
                        put("expected_amount", amount)
                        put("expiration_date", getExpiryTime())
                        put("metadata", JSONObject().apply {
                            put("related_id", relatedId)
                            put("related_type", relatedType)
                        })
                    }
                }

                else -> {
                    onError("Metode pembayaran tidak valid")
                    return
                }
            }

            Log.d(
                "XenditRequest",
                "Method: $paymentMethod, URL: $apiUrl, Body: ${requestBody.toString()}"
            )

            val request = Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", Credentials.basic(BuildConfig.XENDIT_SECRET_KEY, ""))
                .addHeader("Content-Type", "application/json")
                .addHeader("api-version", "2022-07-31")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e("XenditError", "Network failure: ${e.message}")
                    onError("Koneksi gagal: ${e.message}")
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val responseBody = response.body?.string()
                    Log.d("XenditResponse", "Response Code: ${response.code}, Body: $responseBody")
                    if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                        onError("Gagal: ${response.code} - ${response.message} - $responseBody")
                        return
                    }

                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val transactionData = mutableMapOf<String, Any>(
                            "transactionId" to transactionId,
                            "userId" to userId,
                            "amount" to amount,
                            "status" to "PENDING",
                            "paymentMethod" to paymentMethod,
                            "relatedId" to relatedId,
                            "relatedType" to relatedType,
                            "created_at" to System.currentTimeMillis()
                        )

                        when (paymentMethod.uppercase()) {
                            "VA" -> {
                                transactionData["va_number"] =
                                    jsonResponse.optString("account_number")
                                transactionData["bank_code"] = bankCode
                            }

                            "QRIS" -> {
                                transactionData["qris_url"] = jsonResponse.optString("qr_string")
                            }

                            "EWALLET" -> {
                                transactionData["ewallet_ref"] = jsonResponse.optString("id")
                                transactionData["channel_code"] = ewalletChannel
                            }

                            "RETAIL" -> {
                                transactionData["payment_code"] =
                                    jsonResponse.optString("payment_code")
                                transactionData["retail_outlet"] = retailOutlet
                            }
                        }

                        db.collection("transactions").document(transactionId)
                            .set(transactionData)
                            .addOnSuccessListener { onSuccess(transactionId, transactionData) }
                            .addOnFailureListener { e -> onError("Gagal menyimpan: ${e.message}") }
                    } catch (e: JSONException) {
                        Log.e("XenditError", "Parsing error: ${e.message}")
                        onError("Error parsing: ${e.message}")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("XenditError", "Unexpected error: ${e.message}")
            onError("Kesalahan: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getExpiryTime(): String {
        return Instant.now().plus(24, ChronoUnit.HOURS)
            .atZone(ZoneId.of("UTC"))
            .format(DateTimeFormatter.ISO_INSTANT)
    }
}