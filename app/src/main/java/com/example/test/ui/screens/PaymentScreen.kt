package com.example.test.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test.ui.viewModels.PaymentViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.NumberFormat
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    userId: String,
    relatedId: String,
    relatedType: String,
    onPaymentSuccess: (String, Map<String, Any>) -> Unit,
    onPaymentError: (String) -> Unit
) {
    var selectedAmount by remember { mutableStateOf(50000) }
    var selectedPaymentMethod by remember { mutableStateOf("VA") }
    var transactionId by remember { mutableStateOf<String?>(null) }
    var transactionStatus by remember { mutableStateOf("PENDING") }
    var paymentDetails by remember { mutableStateOf<Map<String, Any>?>(null) }
    var expandedMethod by remember { mutableStateOf(false) }
    var expandedAmount by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val mobileNumber = "+6281234567890" // Ganti dengan nomor pengguna dari autentikasi

    LaunchedEffect(transactionId) {
        transactionId?.let { id ->
            FirebaseFirestore.getInstance()
                .collection("transactions")
                .document(id)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        errorMessage = "Error: ${e.message}"
                        onPaymentError("Error: ${e.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.data
                        transactionStatus = data?.get("status") as? String ?: "PENDING"
                        paymentDetails = data
                        if (transactionStatus == "COMPLETED") {
                            data?.let { onPaymentSuccess(id, it) }
                        }
                    }
                }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pembayaran untuk $relatedType", style = MaterialTheme.typography.titleMedium)
        Text("ID: $relatedId", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        val amounts = listOf(50000, 100000, 200000, 500000)
        ExposedDropdownMenuBox(
            expanded = expandedAmount,
            onExpandedChange = { expandedAmount = !expandedAmount }
        ) {
            OutlinedTextField(
                value = "Rp $selectedAmount",
                onValueChange = {},
                readOnly = true,
                label = { Text("Nominal") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAmount) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedAmount,
                onDismissRequest = { expandedAmount = false }
            ) {
                amounts.forEach { amount ->
                    DropdownMenuItem(
                        text = { Text("Rp $amount") },
                        onClick = {
                            selectedAmount = amount
                            expandedAmount = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val paymentMethods = listOf("VA", "QRIS", "EWALLET", "RETAIL")
        ExposedDropdownMenuBox(
            expanded = expandedMethod,
            onExpandedChange = { expandedMethod = !expandedMethod }
        ) {
            OutlinedTextField(
                value = selectedPaymentMethod,
                onValueChange = {},
                readOnly = true,
                label = { Text("Metode Pembayaran") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMethod) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedMethod,
                onDismissRequest = { expandedMethod = false }
            ) {
                paymentMethods.forEach { method ->
                    DropdownMenuItem(
                        text = { Text(method) },
                        onClick = {
                            selectedPaymentMethod = method
                            expandedMethod = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.createTransaction(
                    userId = userId,
                    amount = selectedAmount,
                    paymentMethod = selectedPaymentMethod,
                    relatedId = relatedId,
                    relatedType = relatedType,
                    mobileNumber = if (selectedPaymentMethod == "EWALLET") mobileNumber else null,
                    onSuccess = { id, data ->
                        transactionId = id
                        paymentDetails = data
                        errorMessage = null
                    },
                    onError = { error ->
                        errorMessage = error
                        onPaymentError(error)
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Bayar Sekarang")
        }

        paymentDetails?.let { details ->
            Spacer(modifier = Modifier.height(16.dp))
            Text("Transaction ID: ${details["transactionId"]}")
            Text("Status: $transactionStatus")
            Text("Jumlah: Rp ${details["amount"]}")
            when (selectedPaymentMethod) {
                "VA" -> {
                    Text("Nomor VA: ${details["va_number"]}")
                    Text("Bank: ${details["bank_code"]}")
                }
                "QRIS" -> {
                    details["qris_url"]?.let { url ->
                        QrCodeImage(qrCodeString = url as String)
                    }
                }
                "EWALLET" -> {
                    Text("Referensi: ${details["ewallet_ref"]}")
                    Text("Channel: ${details["channel_code"]}")
                }
                "RETAIL" -> {
                    Text("Kode Pembayaran: ${details["payment_code"]}")
                    Text("Outlet: ${details["retail_outlet"]}")
                }

                else -> {}
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}

// Fungsi QR Code tetap sama
@Composable
fun QrCodeImage(qrCodeString: String) {
    val bitmap = generateQrCode(qrCodeString)
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = Modifier.size(200.dp)
        )
    }
}

fun generateQrCode(text: String): Bitmap? {
    return try {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        Log.e("QrCodeError", "Error generating QR: ${e.message}")
        null
    }
}

// Extension function untuk Toast
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}