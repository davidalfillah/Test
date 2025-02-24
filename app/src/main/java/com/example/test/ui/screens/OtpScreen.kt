package com.example.test.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.test.AuthViewModel

@Composable
fun OtpScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    var otpCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        TextField(value = otpCode, onValueChange = { otpCode = it }, label = { Text("Enter OTP") })
        Button(onClick = {
            authViewModel.verifyOtp(
                otpCode = otpCode,
                onSuccess = { isProfileComplete ->
                    if (isProfileComplete) {
                        navController.navigate("dashboard") // Jika profil sudah lengkap
                    } else {
                        navController.navigate("profile_setup") // Jika belum, isi profil dulu
                    }
                },
                onError = { errorMessage = it }
            )
        }) {
            Text("Verify OTP")
        }
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}
