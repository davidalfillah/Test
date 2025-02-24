package com.example.test.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LogoutDialog(
    showDialog: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirmLogout: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) onDismiss() }, // Tidak bisa ditutup saat loading
            title = { Text("Konfirmasi Logout") },
            text = {
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sedang logout...")
                    }
                } else {
                    Text("Apakah Anda yakin ingin keluar?")
                }
            },
            confirmButton = {
                if (!isLoading) {
                    Button(
                        onClick = onConfirmLogout // ✅ Hanya fungsi, tidak ada pemanggilan Composable di dalamnya
                    ) {
                        Text("Logout")
                    }
                }
            },
            dismissButton = {
                if (!isLoading) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Batal")
                    }
                }
            }
        )
    }
}

