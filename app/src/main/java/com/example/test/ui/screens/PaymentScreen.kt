package com.example.test.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.test.ui.viewModels.PaymentViewModel
import com.example.test.ui.viewModels.Transaction

// Composable untuk PaymentScreen
@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel = viewModel(),
    userId: String,
    userEmail: String,
    navController: NavHostController
) {
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel, userId) {
        viewModel.setCallback(object : PaymentViewModel.Callback {
            override fun onLoading(newIsLoading: Boolean) {
                isLoading = newIsLoading
            }

            override fun onError(message: String?) {
                error = message
            }

            override fun onTransactionsUpdated(newTransactions: List<Transaction>) {
                transactions = newTransactions
            }
        }, userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        }

        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        TransactionSection(transactions, viewModel, userId, userEmail)
        Button(onClick = { navController.navigate("home") }) {
            Text("Kembali ke Home")
        }
    }
}


// Composable untuk TransactionSection
@Composable
fun TransactionSection(
    transactions: List<Transaction>,
    viewModel: PaymentViewModel,
    userId: String,
    userEmail: String
) {
    val context = LocalContext.current
    Column {
        Text("Transaksi Anda:", style = MaterialTheme.typography.headlineSmall)
        transactions.forEach { transaction ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${transaction.category} - Rp${transaction.amount} - ${transaction.status}")
                if (transaction.status == "PENDING" && transaction.paymentUrl != null) {
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(transaction.paymentUrl))
                        context.startActivity(intent)
                    }) {
                        Text("Bayar")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                viewModel.createPayment(
                    category = "donasi",
                    amount = 10000L,
                    userId = userId,
                    email = "davidalfillah@gmail.com",
                    phone = "083822158268",
                    memberId = "GRIB001",
                    referenceId = "REF-12348"
                )
            }) {
                Text("Donasi Rp10.000")
            }
            Button(onClick = {
                viewModel.createPayment(
                    category = "ADVERTISEMENT",
                    amount = 50000L, // Sesuaikan dengan teks
                    userId = userId,
                    email = "davidalfillah@gmail.com",
                    phone = "083822158268",
                    memberId = "GRIB001",
                    referenceId = ""
                )
            }) {
                Text("Iklan Rp50.000")
            }
            Button(onClick = {
                viewModel.createPayment(
                    category = "MEMBERSHIP",
                    amount = 50000L, // Sesuaikan dengan teks
                    userId = userId,
                    email = "davidalfillah@gmail.com",
                    phone = "083822158268",
                    memberId = "GRIB001",
                    referenceId = ""
                )
            }) {
                Text("Iuran Rp50.000")
            }
        }
    }
}