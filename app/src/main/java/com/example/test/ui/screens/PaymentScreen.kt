package com.example.test.ui.screens

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test.ui.viewModels.PaymentViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    userId: String,
    amount: Int,
    onPaymentSuccess: (String, Map<String, Any>) -> Unit,
    onPaymentError: (String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("VA") }
    var mobileNumber by remember { mutableStateOf("") }
    var bankCode by remember { mutableStateOf("BCA") }
    var ewalletChannel by remember { mutableStateOf("ID_OVO") }
    var retailOutlet by remember { mutableStateOf("ALFAMART") }
    var qrisType by remember { mutableStateOf("DYNAMIC") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pilih Metode Pembayaran",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Pilihan metode pembayaran
        PaymentMethodSelector(
            selectedMethod = selectedMethod,
            onMethodSelected = { selectedMethod = it }
        )

        // Form tambahan berdasarkan metode pembayaran
        when (selectedMethod) {
            "VA" -> {
                BankCodeDropdown(
                    selectedBank = bankCode,
                    onBankSelected = { bankCode = it }
                )
            }
            "QRIS" -> {
                QrisTypeSelector(
                    selectedType = qrisType,
                    onTypeSelected = { qrisType = it }
                )
            }
            "EWALLET" -> {
                EwalletSelector(
                    selectedChannel = ewalletChannel,
                    onChannelSelected = { ewalletChannel = it }
                )
                if (ewalletChannel == "ID_OVO") {
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("Nomor HP") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            }
            "RETAIL" -> {
                RetailOutletDropdown(
                    selectedOutlet = retailOutlet,
                    onOutletSelected = { retailOutlet = it }
                )
            }
        }

        // Tombol Bayar
        Button(
            onClick = {
                isLoading = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    viewModel.createTransaction(
                        userId = userId,
                        amount = amount,
                        paymentMethod = selectedMethod,
                        mobileNumber = if (selectedMethod == "EWALLET" && ewalletChannel == "ID_OVO") mobileNumber else null,
                        bankCode = bankCode,
                        ewalletChannel = ewalletChannel,
                        retailOutlet = retailOutlet,
                        qrisType = qrisType,
                        onSuccess = { transactionId, data ->
                            isLoading = false
                            onPaymentSuccess(transactionId, data)
                        },
                        onError = { error ->
                            isLoading = false
                            onPaymentError(error)
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Bayar Sekarang - Rp ${amount.formatAsCurrency()}")
            }
        }
    }
}

@Composable
fun PaymentMethodSelector(
    selectedMethod: String,
    onMethodSelected: (String) -> Unit
) {
    val methods = listOf("VA" to "Virtual Account", "QRIS" to "QRIS", "EWALLET" to "E-Wallet", "RETAIL" to "Retail Outlet")

    Column {
        methods.forEach { (key, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMethodSelected(key) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMethod == key,
                    onClick = { onMethodSelected(key) }
                )
                Text(text = label, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun BankCodeDropdown(selectedBank: String, onBankSelected: (String) -> Unit) {
    val banks = listOf("BCA", "BRI", "BNI", "MANDIRI")
    DropdownMenuComponent("Pilih Bank", banks, selectedBank, onBankSelected)
}

@Composable
fun EwalletSelector(selectedChannel: String, onChannelSelected: (String) -> Unit) {
    val ewalletOptions = listOf("ID_OVO" to "OVO", "ID_DANA" to "DANA", "ID_SHOPEEPAY" to "ShopeePay")
    DropdownMenuComponent(
        label = "Pilih E-Wallet",
        options = ewalletOptions.map { it.second },
        selectedOption = ewalletOptions.find { it.first == selectedChannel }?.second ?: "",
        onOptionSelected = { selected -> onChannelSelected(ewalletOptions.find { it.second == selected }!!.first) }
    )
}

@Composable
fun RetailOutletDropdown(selectedOutlet: String, onOutletSelected: (String) -> Unit) {
    val outlets = listOf("ALFAMART", "INDOMARET")
    DropdownMenuComponent("Pilih Outlet", outlets, selectedOutlet, onOutletSelected)
}

@Composable
fun QrisTypeSelector(selectedType: String, onTypeSelected: (String) -> Unit) {
    val types = listOf("DYNAMIC" to "Dinamis", "STATIC" to "Statis")
    DropdownMenuComponent(
        label = "Tipe QRIS",
        options = types.map { it.second },
        selectedOption = types.find { it.first == selectedType }?.second ?: "",
        onOptionSelected = { selected -> onTypeSelected(types.find { it.second == selected }!!.first) }
    )
}

@Composable
fun DropdownMenuComponent(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { if (enabled) expanded = true },
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    modifier = Modifier
                        .clickable(enabled = enabled) { expanded = true }
                        .rotate(if (expanded) 180f else 0f) // Animasi rotasi ikon
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) }, // Parameter text untuk konten utama
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    leadingIcon = {
                        // Opsional: Tambahkan ikon di depan setiap opsi jika diinginkan
                        // Icon(Icons.Default.Check, contentDescription = null, tint = if (option == selectedOption) MaterialTheme.colorScheme.primary else Color.Transparent)
                    },
                    trailingIcon = {
                        // Opsional: Tambahkan ikon di belakang setiap opsi jika diinginkan
                        if (option == selectedOption) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = MenuDefaults.itemColors(), // Menggunakan warna default Material
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DropdownMenuComponentPreview() {
    val options = listOf("BCA", "BRI", "BNI", "MANDIRI")
    var selectedOption by remember { mutableStateOf(options[0]) }

    DropdownMenuComponent(
        label = "Pilih Bank",
        options = options,
        selectedOption = selectedOption,
        onOptionSelected = { selectedOption = it }
    )
}

// Extension function untuk format currency
fun Int.formatAsCurrency(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(this).replace("Rp", "Rp ").replace(",00", "")
}

// Penggunaan
@Preview
@Composable
fun PaymentScreenPreview() {
    val viewModel = viewModel<PaymentViewModel>()
    PaymentScreen(
        viewModel = viewModel,
        userId = "user123",
        amount = 100000,
        onPaymentSuccess = { _, _ -> },
        onPaymentError = { }
    )
}
