package com.example.test.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
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
fun UploadKtpScreen(navController: NavController) {
    val context = LocalContext.current
    val cameraPermission = Manifest.permission.CAMERA

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Launcher untuk membuka galeri
    val getImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imageUri = it }
    }

    // Launcher untuk mengambil foto dari kamera
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { capturedBitmap ->
            capturedBitmap?.let {
                bitmap = it
                imageUri = saveBitmapToCache(context, it) // Simpan ke cache dan dapatkan URI
            }
        }
    )

    // Launcher untuk meminta izin kamera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                takePictureLauncher.launch()
            } else {
                Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(imageUri) {
        imageUri?.let { uri ->
            isProcessing = true
            processImage(context, uri) { extractedNik, extractedName,extractedBirthDate, extractedAddress ->
//                uploadImageToFirebase(context, uri) { imageUrl ->
//                    navController.navigate("registration_screen?nik=$extractedNik&name=$extractedName&address=$extractedAddress&imageUrl=$imageUrl")
//                }
                navController.navigate("registerGrib?nik=$extractedNik&name=$extractedName&address=$extractedAddress&imageUrl=&birthDate=$extractedBirthDate")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tata Cara Upload KTP", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Preview KTP",
                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp))
            )
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
                    takePictureLauncher.launch()
                } else {
                    permissionLauncher.launch(cameraPermission)
                }
            }) {
                Text("Foto KTP")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (imageUri != null && !isProcessing) {
            Button(
                onClick = {
                    isProcessing = true
                    processImage(context, imageUri!!) { extractedNik, extractedName, extractedBirthDate, extractedAddress ->
//                        uploadImageToFirebase(
//                            context,
//                            imageUri = imageUri!!
//                        ) { imageUrl ->
//                            navController.navigate("registration_screen?nik=$extractedNik&name=$extractedName&address=$extractedAddress&imageUrl=$imageUrl")
//                        }

                        navController.navigate("registerGrib?nik=$extractedNik&name=$extractedName&address=$extractedAddress&imageUrl=&birthDate=$extractedBirthDate")
                    }
                }
            ) {
                Text("Lanjutkan ke Registrasi")
            }
        }
    }
}

private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
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

private fun processImage(context: Context, imageUri: Uri, onExtracted: (String, String, String, String) -> Unit) {
    try {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromFilePath(context, imageUri)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text
                Log.d("MLKit", "Hasil OCR:\n$rawText") // ✅ Debug: Menampilkan hasil OCR

                val extractedData = extractData(rawText)

                val nik = extractedData.getOrNull(0) ?: "NIK tidak ditemukan"
                val name = extractedData.getOrNull(1) ?: "Nama tidak ditemukan"
                val birthDate = extractedData.getOrNull(2) ?: "Tanggal lahir tidak ditemukan"
                val address = extractedData.getOrNull(3) ?: "Alamat tidak ditemukan"

                Log.d("MLKit", "NIK: $nik, Nama: $name, Alamat: $address, Birthday: $birthDate") // ✅ Debug hasil ekstraksi

                // 🔹 Pastikan dijalankan di thread utama agar UI diperbarui
                Handler(Looper.getMainLooper()).post {
                    onExtracted(nik, name, birthDate, address)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MLKit", "Gagal mengenali teks", e)
            }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}



private fun extractData(text: String): List<String> {
    val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

    val labels = mutableListOf<String>()
    val values = mutableListOf<String>()

    var isCapturingValues = false

    for (line in lines) {
        if (":" in line) {
            values.add(line.substringAfter(":").trim()) // Simpan sebagai nilai
            isCapturingValues = true
        } else {
            if (!isCapturingValues) {
                labels.add(line) // Simpan sebagai label
            } else {
                values.add(line) // Simpan sebagai nilai tambahan
            }
        }
    }


    // **Step 2: Ambil Data**
    val nik = values.getOrNull(0) ?: "NIK tidak ditemukan"
    val nama = values.getOrNull(1) ?: "Nama tidak ditemukan"
    val birthDate = values.getOrNull(2) ?: "Tanggal lahir tidak ditemukan"

    // **Ambil alamat dari index 4 hingga 6 jika ada**
    val alamat = values.getOrNull(4) ?: "Alamat tidak ditemukan"

    // **Tambahkan RT/RW jika ada**
    val rtRw = values.getOrNull(5)?.let { " RT/RW $it" } ?: ""

    return listOf(
        nik,
        nama,
        birthDate,
        alamat + rtRw
    )
}
