package com.example.test.ui.screens

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.common.InputImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.ui.draw.clip
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.functions
import com.google.firebase.storage.storage
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.nio.ByteBuffer

// Data class untuk menyimpan hasil OCR dan gambar, implementasi Parcelable
data class KtpResult(
    val image: Bitmap?,
    val nik: String,
    val name: String,
    val address: String,
    val fullText: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        image = BitmapFactory.decodeByteArray(
            parcel.createByteArray() ?: ByteArray(0),
            0,
            parcel.readInt()
        ),
        nik = parcel.readString() ?: "",
        name = parcel.readString() ?: "",
        address = parcel.readString() ?: "",
        fullText = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        val stream = java.io.ByteArrayOutputStream()
        image?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        parcel.writeInt(byteArray.size)
        parcel.writeByteArray(byteArray)
        parcel.writeString(nik)
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeString(fullText)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<KtpResult> {
        override fun createFromParcel(parcel: Parcel): KtpResult = KtpResult(parcel)
        override fun newArray(size: Int): Array<KtpResult?> = arrayOfNulls(size)
    }
}

// Extension function untuk mengkonversi ImageProxy ke Bitmap
fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

// Screen Kamera
@Composable
fun KtpScannerScreen(
    onResult: (KtpResult) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val preview = remember {
        Preview.Builder()
            .setTargetRotation(Surface.ROTATION_0) // Mengunci ke landscape
            .build()
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setTargetRotation(Surface.ROTATION_0) // Mengunci ke landscape
            .build()
    }
    val executor = ContextCompat.getMainExecutor(context)
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val storage = Firebase.storage
    val functions = Firebase.functions
    val ktpAspectRatio = 8.56f / 5.398f

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            Log.e("AuthCheck", "User is not authenticated!")
            Toast.makeText(context, "User not authenticated!", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("AuthCheck", "User authenticated: ${currentUser.uid}")
            Toast.makeText(context, "User authenticated: ${currentUser.uid}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
        }, executor)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .aspectRatio(ktpAspectRatio)
                .clip(RoundedCornerShape(16.dp)),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                    preview.setSurfaceProvider(this.surfaceProvider)
                }
            }
        )

        Button(
            onClick = {
                imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        val bitmap = image.toBitmap()
                        val adjustedBitmap = if (image.imageInfo.rotationDegrees != 0) {
                            val matrix = Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
                            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                        } else bitmap

                        // Unggah ke Firebase Storage
                        val baos = ByteArrayOutputStream()
                        adjustedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()
                        val fileName = "ktp-${System.currentTimeMillis()}.jpg"
                        val storageRef = storage.reference.child("ktp-images/$fileName")

                        storageRef.putBytes(data)
                            .addOnSuccessListener {
                                // Panggil Cloud Function setelah unggahan sukses
                                val requestData = hashMapOf("filePath" to "ktp-images/$fileName")
                                functions.getHttpsCallable("processKtpImage")
                                    .call(requestData)
                                    .addOnSuccessListener { result ->
                                        val data = result.data as Map<*, *>
                                        onResult(KtpResult(
                                            image = adjustedBitmap,
                                            nik = data["nik"] as String,
                                            name = data["name"] as String,
                                            address = data["address"] as String,
                                            fullText = data["fullText"] as String
                                        ))
                                    }
                                    .addOnFailureListener { e ->
                                        onResult(KtpResult(adjustedBitmap, "", "", "", "Error: ${e.message}"))
                                    }
                            }
                            .addOnFailureListener { e ->
                                onResult(KtpResult(adjustedBitmap, "", "", "", "Upload Error: ${e.message}"))
                            }

                        image.close()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        onResult(KtpResult(null, "", "", "", "Capture Error: ${exception.message}"))
                    }
                })
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(0.5f)
        ) {
            Text("Capture")
        }

        IconButton(
            onClick = { /* Kembali */ },
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

// Screen Hasil
@Composable
fun KtpResultScreen(result: KtpResult) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        result.image?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Captured KTP",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(8.56f / 5.398f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        }
//        Text(
//            text = "Full: ${result.fullText}",
//            style = MaterialTheme.typography.bodyLarge,
//            fontWeight = FontWeight.Bold
//        )
        Text(
            text = "NIK: ${result.nik}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Nama: ${result.name}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Alamat: ${result.address}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}


// Kelas Helper untuk OCR dan Image Processing
object KtpImageAnalyzer {
    data class OcrBlock(val text: String, val x: Float, val y: Float)

    private fun parseKtpData(
        blocks: List<OcrBlock>,
        nik: MutableState<String>,
        name: MutableState<String>,
        address: MutableState<String>
    ) {
        // Daftar kemungkinan variasi label berdasarkan hasil OCR
        val nikLabels = listOf("nik", "nik:")
        val nameLabels = listOf("nama", "nama:", "nema:", "aama", "nema", "name", "name:")
        val addressLabels = listOf("alamat", "alamat:", "lamat", "lamat:", "alaahom", "alaahom:", "alama", "alama:", "alamao", "alamao:", "alamarw", "alamarw:")
        val rtRwLabels = listOf("rt/rw", "rt/rw:", "rw ", "rw:", "rt ", "rt:", "rtrw", "rtrw:", "rtirw ", "rtirw:", "rtaw:", "rtaw")

        var foundName = false
        var foundNik = false
        var foundAddress = false
        var foundRtRw = false
        var tempAddress = ""
        var tempRtRw = ""

        fun cleanText(text: String): String {
            return text.trim().removePrefix(":").removePrefix("*")
        }

        for (i in blocks.indices) {
            val block = blocks[i]
            val blockText = block.text.lowercase()  // Konversi teks ke lowercase untuk pencocokan lebih fleksibel

            // Cari Label "NIK"
            if (!foundNik && nikLabels.any { blockText.contains(it) }) {
                val labelY = block.y
                val potentialNik = blocks.find { it.y in (labelY - 5)..(labelY + 5) && it.x > block.x }
                if (potentialNik != null) {
                    nik.value = cleanText(potentialNik.text)
                    foundNik = true
                    Log.d("PARSED_NIK", "NIK ditemukan: ${nik.value}")
                }
            }

            // Cari Label "Nama"
            if (!foundName && nameLabels.any { blockText.contains(it) }) {
                val labelY = block.y
                val potentialName = blocks.find { it.y in (labelY - 5)..(labelY + 5) && it.x > block.x }
                if (potentialName != null) {
                    name.value = cleanText(potentialName.text)
                    foundName = true
                    Log.d("PARSED_NAME", "Nama ditemukan: ${name.value}")
                }
            }

            // Cari Label "Alamat"
            if (!foundAddress && addressLabels.any { blockText.contains(it) }) {
                val labelY = block.y
                val potentialAddress = blocks.find { it.y in (labelY - 5)..(labelY + 5) && it.x > block.x }
                if (potentialAddress != null) {
                    tempAddress = cleanText(potentialAddress.text)
                    foundAddress = true
                    Log.d("PARSED_ADDRESS", "Alamat ditemukan: $tempAddress")
                }
            }

            // Cari Label "RT/RW"
            if (!foundRtRw && rtRwLabels.any { blockText.contains(it) }) {
                val labelY = block.y
                val potentialRtRw = blocks.find { it.y in (labelY - 5)..(labelY + 5) && it.x > block.x }
                if (potentialRtRw != null) {
                    tempRtRw = cleanText(potentialRtRw.text)
                    foundRtRw = true
                    Log.d("PARSED_RT_RW", "RT/RW ditemukan: $tempRtRw")
                }
            }

            // Berhenti lebih awal jika semua data sudah ditemukan
            if (foundNik && foundName && foundAddress && foundRtRw) break
        }

        // Pastikan RT/RW memiliki format yang benar
        val formattedRtRw = when {
            tempRtRw.contains("/") -> tempRtRw // Sudah memiliki "/"
            tempRtRw.length == 6 && tempRtRw.all { it.isDigit() } -> {
                "${tempRtRw.substring(0, 3)}/${tempRtRw.substring(3, 6)}"
            }
            tempRtRw.isNotEmpty() -> {
                val splitRtRw = tempRtRw.trim().split(" ")
                if (splitRtRw.size == 2) "${splitRtRw[0]}/${splitRtRw[1]}" else tempRtRw
            }
            else -> ""
        }

        // Gabungkan alamat dan RT/RW jika keduanya ditemukan
        if (foundAddress || foundRtRw) {
            address.value = if (foundAddress && formattedRtRw.isNotEmpty()) {
                "$tempAddress, RT/RW: $formattedRtRw"
            } else tempAddress

            Log.d("PARSED_FULL_ADDRESS", "Alamat Lengkap: ${address.value}")
        }

        // Logging jika ada data yang tidak ditemukan
        if (!foundNik) Log.e("PARSED_NIK", "NIK tidak ditemukan!")
        if (!foundName) Log.e("PARSED_NAME", "Nama tidak ditemukan!")
        if (!foundAddress) Log.e("PARSED_ADDRESS", "Alamat tidak ditemukan!")
        if (!foundRtRw) Log.e("PARSED_RT_RW", "RT/RW tidak ditemukan!")
    }

    // Fungsi untuk mem-parse data KTP dan mengembalikan KtpResult
    fun parseKtpData(blocks: List<OcrBlock>, fullText: String, image: Bitmap): KtpResult {
        val nik = mutableStateOf("")
        val name = mutableStateOf("")
        val address = mutableStateOf("")
        parseKtpData(blocks, nik, name, address)
        return KtpResult(
            image = image,
            nik = nik.value,
            name = name.value,
            address = address.value,
            fullText = fullText
        )
    }
}

fun enhanceImage(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    val rotatedBitmap = if (rotationDegrees != 0) {
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else bitmap

    val targetWidth = 640
    val targetHeight = (targetWidth * (rotatedBitmap.height.toFloat() / rotatedBitmap.width)).toInt()
    val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, targetWidth, targetHeight, true)

    val mutableBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
            setSaturation(0.8f)
            postConcat(ColorMatrix().apply {
                set(floatArrayOf(
                    1.2f, 0f, 0f, 0f, 20f,
                    0f, 1.2f, 0f, 0f, 20f,
                    0f, 0f, 1.2f, 0f, 20f,
                    0f, 0f, 0f, 1f, 0f
                ))
            })
        })
    }
    canvas.drawBitmap(mutableBitmap, 0f, 0f, paint)
    return mutableBitmap
}