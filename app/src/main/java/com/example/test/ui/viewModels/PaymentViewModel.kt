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
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
    private var transactionListener: ListenerRegistration? = null
    private val client = OkHttpClient()

    @RequiresApi(Build.VERSION_CODES.O)
    fun createTransaction(
        userId: String,
        amount: Int,
        paymentMethod: String,
        mobileNumber: String? = null,
        bankCode: String = "BCA",
        ewalletChannel: String = "ID_OVO",
        retailOutlet: String = "ALFAMART",
        qrisType: String = "DYNAMIC", // Tambahan untuk QRIS STATIC
        onSuccess: (String, Map<String, Any>) -> Unit,
        onError: (String) -> Unit
    ) {
        val transactionId = "TEST${System.currentTimeMillis()}"
        val requestBody = JSONObject()
        var apiUrl = ""

        try {
            when (paymentMethod.uppercase()) {
                "VA" -> {
                    apiUrl = "https://api.xendit.co/callback_virtual_accounts"
                    requestBody.apply {
                        put("external_id", transactionId)
                        put("bank_code", bankCode)
                        put("name", "User $userId")
                        put("expected_amount", amount)
                        put("is_single_use", true)
                        put("expiration_date", getExpiryTime())
                    }
                }
                "QRIS" -> {
                    apiUrl = "https://api.xendit.co/qr_codes"
                    requestBody.apply {
                        put("reference_id", transactionId)
                        put("type", qrisType.uppercase())
                        put("currency", "IDR")
                        if (qrisType.uppercase() == "DYNAMIC") {
                            put("amount", amount)
                        }
                        put("expires_at", getExpiryTime())
                    }
                }
                "EWALLET" -> {
                    if (mobileNumber.isNullOrEmpty() && ewalletChannel == "ID_OVO") {
                        onError("Nomor HP diperlukan untuk OVO")
                        return
                    }
                    apiUrl = "https://api.xendit.co/ewallets/charges"
                    requestBody.apply {
                        put("reference_id", transactionId)
                        put("currency", "IDR")
                        put("amount", amount)
                        put("checkout_method", "ONE_TIME_PAYMENT")
                        put("channel_code", ewalletChannel)
                        put("channel_properties", JSONObject().apply {
                            if (mobileNumber != null) put("mobile_number", mobileNumber)
                            when (ewalletChannel) {
                                "ID_DANA", "ID_SHOPEEPAY" -> {
                                    put("success_redirect_url", "yourapp://success")
                                    put("failure_redirect_url", "yourapp://failure")
                                }
                                "ID_OVO" -> {
                                    put("success_redirect_url", "yourapp://success")
                                }
                            }
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
                    }
                }
                else -> {
                    onError("Metode pembayaran tidak valid")
                    return
                }
            }

            val request = Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", Credentials.basic(BuildConfig.XENDIT_SECRET_KEY, ""))
                .addHeader("Content-Type", "application/json")
                .addHeader("api-version", "2022-07-31")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e("TransactionError", "Network error: ${e.message}")
                    onError("Koneksi gagal: ${e.message}")
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val responseBody = response.body?.string()

                    if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                        Log.e("TransactionError", "Error: ${response.code} - ${response.message}")
                        onError("Gagal: ${response.code} - ${response.message}")
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
                            "created_at" to System.currentTimeMillis()
                        )

                        when (paymentMethod.uppercase()) {
                            "VA" -> {
                                transactionData["va_number"] = jsonResponse.optString("account_number")
                                transactionData["bank_code"] = bankCode
                            }
                            "QRIS" -> {
                                transactionData["qris_url"] = jsonResponse.optString("qr_string")
                                transactionData["qris_type"] = qrisType
                            }
                            "EWALLET" -> {
                                transactionData["ewallet_ref"] = jsonResponse.optString("id")
                                transactionData["channel_code"] = ewalletChannel
                                if (mobileNumber != null) transactionData["mobile_number"] = mobileNumber
                            }
                            "RETAIL" -> {
                                transactionData["payment_code"] = jsonResponse.optString("payment_code")
                                transactionData["retail_outlet"] = retailOutlet
                            }
                        }

                        db.collection("transactions").document(transactionId)
                            .set(transactionData)
                            .addOnSuccessListener {
                                onSuccess(transactionId, transactionData)
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirestoreError", "Error: ${e.message}")
                                onError("Gagal menyimpan: ${e.message}")
                            }

                    } catch (e: JSONException) {
                        Log.e("JSONError", "Parsing error: ${e.message}")
                        onError("Error parsing: ${e.message}")
                    }
                }
            })

        } catch (e: Exception) {
            Log.e("TransactionError", "Unexpected error: ${e.message}")
            onError("Kesalahan: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getExpiryTime(): String {
        return Instant.now()
            .plus(24, ChronoUnit.HOURS)
            .atZone(ZoneId.of("UTC"))
            .format(DateTimeFormatter.ISO_INSTANT)
    }

    override fun onCleared() {
        super.onCleared()
        transactionListener?.remove()
    }
}