package com.example.test.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.test.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileSetupScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    var name by remember { mutableStateOf("") }
    var profilePicUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val user = FirebaseAuth.getInstance().currentUser

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Lengkapi Profil", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Lengkap") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isNotEmpty() && user != null) {
                    isLoading = true
                    val userRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    userRef.update("name", name, "profilePicUrl", profilePicUrl, "isProfileComplete", true)
                        .addOnSuccessListener {
                            isLoading = false
                            navController.navigate("dashboard")
                        }
                        .addOnFailureListener {
                            isLoading = false
                        }
                }
            }
        ) {
            Text("Simpan")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}
