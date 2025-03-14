package com.example.test.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileSetupScreen(navController: NavHostController, authViewModel: AuthViewModel, paddingValues: PaddingValues) {
    var name by remember { mutableStateOf("") }
    var profilePicUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val user = FirebaseAuth.getInstance().currentUser

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
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
                text = "Sedikit Lagi",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            // Subtitle
            Text(
                text = "Lengkapi data diri anda.",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // OTP Input
            OutlinedTextField(
                value = name,
                onValueChange = { newText ->
                    val formattedText = newText.replace(Regex("\\s+"), " ") // Mengganti spasi ganda dengan satu spasi
                    if (formattedText.matches(Regex("^[A-Za-z][A-Za-z ]*$"))) {
                        name = formattedText.trimStart() // Mencegah spasi di awal
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text
                ),
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(modifier = Modifier.height(24.dp))
            // Tombol Verifikasi
            Button(
                onClick = {
                    if (name.isNotEmpty() && user != null) {
                        isLoading = true
                        val userRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
                        userRef.update("name", name, "profilePicUrl", profilePicUrl, "isProfileComplete", true)
                            .addOnSuccessListener {
                                isLoading = false
                                navController.navigate("home") {
                                    popUpTo(0) // Hapus semua riwayat navigasi
                                }
                            }
                            .addOnFailureListener {
                                isLoading = false
                            }
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
                    Text("Simpan")
                }
            }
        }


    }
}

