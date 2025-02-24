package com.example.test.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.test.AuthViewModel

@SuppressLint("ContextCastToActivity")
@Composable
fun LoginScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    var phoneNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val activity = LocalContext.current as Activity

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login dengan Nomor HP", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Nomor Telepon") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            authViewModel.sendOtp(
                phoneNumber = phoneNumber,
                activity = activity,
                onSuccess = { navController.navigate("otp_screen") },
                onError = { errorMessage = it }
            )
        }) {
            Text("Kirim OTP")
        }

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
