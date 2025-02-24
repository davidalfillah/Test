package com.example.test.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.content.MediaType.Companion.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.test.AuthViewModel
import com.example.test.R
import com.example.test.ui.components.rememberImeState


@SuppressLint("ContextCastToActivity")
@Composable
fun LoginScreen(navController: NavHostController, authViewModel: AuthViewModel, paddingValues: PaddingValues) {
    var phoneNumber by remember { mutableStateOf("+62") }
    var errorMessage by remember { mutableStateOf("") }
    val imeState = rememberImeState() // Mengecek apakah keyboard muncul
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

        // Kolom utama
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.BottomCenter)
                .imePadding(), // Agar UI naik saat keyboard muncul
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f)) // Mengurangi jarak kosong

            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.grib_02),
                contentDescription = "Logo",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Menghindari logo terlalu kecil
                    .aspectRatio(3f) // Menjaga proporsi lebar logo
                    .padding(bottom = 8.dp) // Mengurangi jarak dengan teks
            )

            // Title
            Text(
                text = "Bergabung dengan kami dan tebar kebaikan",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 33.sp

            )

            // Subtitle
            Text(
                text = "Masukkan nomor telepon kamu untuk melanjutkan.",
                fontSize = 16.sp,
                color = Color.Gray
            )

            // Input Nomor Telepon
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    if (it.startsWith("+62")) { // Cegah penghapusan kode negara
                        phoneNumber = it
                    } else {
                        phoneNumber = "+62" // Jika pengguna menghapus, tetap kembalikan kode negara
                    }
                    errorMessage = "" // Hapus pesan error jika pengguna mengetik ulang
                },
                label = { Text("Nomor Telepon") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone
                ),
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage.isNotEmpty()
            )

            // Pesan Error
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
            }

            // Tombol Selanjutnya
            Button(
                onClick = {
                    if (phoneNumber.length < 10) {
                        errorMessage = "Nomor telepon tidak valid!"
                    } else {
                        isLoading = true // Tampilkan loading sebelum proses OTP
                        authViewModel.sendOtp(
                            phoneNumber = phoneNumber,
                            onSuccess = { verificationId ->
                                isLoading = false
                                navController.navigate("otp_screen/$phoneNumber")
                            },
                            activity = activity,
                            onError = { errorMsg ->
                                isLoading = false
                                errorMessage = errorMsg
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
                    Text(text = "Selanjutnya", fontSize = 18.sp)
                }
            }


            Spacer(modifier = Modifier.height(48.dp)) // Agar tombol tidak terlalu bawah
        }
    }
}


