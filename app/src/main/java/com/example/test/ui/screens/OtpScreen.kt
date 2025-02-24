package com.example.test.ui.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.test.AuthViewModel
import com.example.test.R
import com.example.test.ui.components.rememberImeState

@Composable
fun OtpScreen(navController: NavHostController, phoneNumber: String, authViewModel: AuthViewModel, paddingValues: androidx.compose.foundation.layout.PaddingValues) {
    var otpCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val imeState = rememberImeState() // Untuk deteksi keyboard aktif
    val context = LocalContext.current
    val activity = context as Activity
    var isLoading by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Tombol back mengambang
        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .align(Alignment.TopStart),
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .imePadding(), // Agar UI naik saat keyboard muncul
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Title
            Text(
                text = "Masukkan Kode OTP",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            // Subtitle
            Text(
                text = "Kami telah mengirimkan kode ke nomor Anda",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // OTP Input
            OutlinedTextField(
                value = otpCode,
                onValueChange = {
                    if (it.length <= 6) otpCode = it
                    errorMessage = "" // Hapus pesan error jika pengguna mengetik ulang
                },
                label = { Text("Kode OTP") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.NumberPassword
                ),
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage.isNotEmpty()
            )

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Verifikasi
            Button(
                onClick = {
                    if (otpCode.length < 6) {
                        errorMessage = "Kode OTP harus 6 digit"
                    } else {
                        isLoading = true // Aktifkan loading sebelum proses verifikasi
                        authViewModel.verifyOtp(
                            otpCode,
                            onSuccess = { isProfileComplete ->
                                isLoading = false
                                if (isProfileComplete) {

                                    navController.navigate("success?nextScreen=home") {
                                        popUpTo(0) { inclusive = true } // Hapus seluruh backstack
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate("success?nextScreen=profile_setup") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = error // Tampilkan pesan kesalahan
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading // Nonaktifkan tombol saat loading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Verifikasi")
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Teks Kirim Ulang OTP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Belum menerima kode? ")
                Text(
                    text = "Kirim ulang",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        authViewModel.resendOtp(
                            phoneNumber,
                            activity, // Pastikan activity tersedia
                            onSuccess = { success ->
                                if (success) {
                                    errorMessage = "Kode OTP telah dikirim ulang."
                                }
                            },
                            onError = { error ->
                                errorMessage = error
                            }
                        )
                    }
                )
            }
        }


        }
    }

