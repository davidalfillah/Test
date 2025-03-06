package com.example.test.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.example.test.R
import com.example.test.ui.viewModels.MemberViewModel
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException
import java.util.UUID

@Composable
fun UploadKtpScreen(navController: NavController, viewModel: MemberViewModel) {
    val context = LocalContext.current
    val cameraPermission = Manifest.permission.CAMERA

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var ktpData by remember { mutableStateOf<MemberViewModel.KTPData?>(null) }

    val getImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                navController.navigate("customCamera")
            } else {
                Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Proses gambar saat imageUri berubah
    LaunchedEffect(imageUri) {
        imageUri?.let { uri ->
            isProcessing = true
            val imageBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            viewModel.scanKTP(
                bitmap = imageBitmap,
                onResult = { result ->
                    isProcessing = false
                    ktpData = result // Simpan hasil OCR
                },
                onError = { error ->
                    isProcessing = false
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tata Cara Upload KTP", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Preview KTP",
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tampilkan hasil OCR jika ada
            ktpData?.let { data ->
                Column(horizontalAlignment = Alignment.Start) {
                    Text("NIK: ${data.nik}", fontSize = 14.sp)
                    Text("Nama: ${data.nama}", fontSize = 14.sp)
                    Text("Tanggal Lahir: ${data.tempatTanggalLahir}", fontSize = 14.sp)
                    Text("Alamat: ${data.alamat}", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        navController.navigate(
                            "registerGrib?nik=${data.nik}&name=${data.nama}&address=${data.alamat}&imageUrl=$imageUri&birthDate=${data.tempatTanggalLahir}"
                        )
                    }
                ) {
                    Text("Selanjutnya")
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(8.dp))
                    .clickable { getImage.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Text("Pilih atau Foto KTP", color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { getImage.launch("image/*") }) {
                Text("Pilih Gambar KTP")
            }

            Button(onClick = {
                if (ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
                    navController.navigate("customCamera")
                } else {
                    permissionLauncher.launch(cameraPermission)
                }
            }) {
                Text("Foto KTP")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isProcessing) {
            Text("Memproses KTP...", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "ktp_image.jpg")
    file.outputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
    }
    return file.toUri()
}



//private fun uploadImageToFirebase(context: Context, imageUri: Uri, onComplete: (String?) -> Unit) {
//    val storageRef = Firebase.storage.reference.child("ktp_images/${UUID.randomUUID()}.jpg")
//
//    storageRef.putFile(imageUri)
//        .addOnSuccessListener {
//            storageRef.downloadUrl.addOnSuccessListener { uri ->
//                onComplete(uri.toString()) // URL berhasil didapatkan
//            }.addOnFailureListener {
//                Toast.makeText(context, "Gagal mendapatkan URL, lanjut tanpa upload", Toast.LENGTH_SHORT).show()
//                onComplete(null) // Lanjut tanpa URL
//            }
//        }
//        .addOnFailureListener {
//            Toast.makeText(context, "Upload gagal, lanjut tanpa gambar", Toast.LENGTH_SHORT).show()
//            onComplete(null) // Lanjut tanpa upload
//        }
//}


//https://scanktp-4utu2r7iya-uc.a.run.app
