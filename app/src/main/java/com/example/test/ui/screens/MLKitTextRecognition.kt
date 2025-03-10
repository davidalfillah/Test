package com.example.test.ui.screens

import android.annotation.SuppressLint
import android.content.Context
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
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

@Composable
fun MLKitKtpScanner() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val extractedText = remember { mutableStateOf("") }
    val nik = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        KtpScannerView(
            context = context,
            lifecycleOwner = lifecycleOwner,
            extractedText = extractedText,
            nik = nik,
            name = name,
                    address = address
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(text = "Full Text: ${extractedText.value}")
            Text(text = "NIK: ${nik.value}", fontWeight = FontWeight.Bold)
            Text(text = "Nama: ${name.value}", fontWeight = FontWeight.Bold)
            Text(text = "Alamat: ${address.value}", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun KtpScannerView(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    extractedText: MutableState<String>,
    nik: MutableState<String>,
    name: MutableState<String>,
    address: MutableState<String>
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    val executor = ContextCompat.getMainExecutor(context)
    val cameraProvider = cameraProviderFuture.get()


    // Menggunakan On-Device Text Recognizer
    val textRecognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)) // Latar belakang gelap
    ) {
        // Overlay kotak KTP
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f) // Kamera hanya sebesar area KTP
                .aspectRatio(8.56f / 5.398f) // Rasio KTP
                .align(Alignment.Center) // Posisi di tengah
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black) // Background untuk memastikan kotak terlihat
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    cameraProviderFuture.addListener({
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .apply {
                                setAnalyzer(
                                    cameraExecutor,
                                    KtpImageAnalyzers(textRecognizer, extractedText, nik, name, address)
                                )
                            }
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            imageAnalysis,
                            preview
                        )
                    }, executor)
                    preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    previewView
                }
            )
        }

        // Tombol kembali
        IconButton(
            onClick = { Toast.makeText(context, "Back Clicked", Toast.LENGTH_SHORT).show() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "back",
                tint = Color.White
            )
        }
    }
}




class KtpImageAnalyzers(
    private val textRecognizer: TextRecognizer,
    private val extractedText: MutableState<String>,
    private val nik: MutableState<String>,
    private val name: MutableState<String>,
    private val address: MutableState<String>
) : ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        // Konversi ImageProxy ke Bitmap
        val bitmap = imageProxy.toBitmap()

        // Pra-pemrosesan dan resize gambar
        val enhancedBitmap = enhanceAndResizeImage(bitmap, rotationDegrees)

        // Konversi ke InputImage untuk ML Kit
        val image = enhancedBitmap?.let { InputImage.fromBitmap(it, 0) }

        if (image != null) {
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val blocks = mutableListOf<OcrBlock>()

                    // Iterasi setiap blok hasil OCR
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val ocrBlock = OcrBlock(
                                text = line.text,
                                x = line.boundingBox?.left?.toFloat() ?: 0f,
                                y = line.boundingBox?.top?.toFloat() ?: 0f
                            )
                            blocks.add(ocrBlock)

                            // Log hasil blok OCR
                            Log.d("OCR_BLOCK", "Text: '${ocrBlock.text}', X: ${ocrBlock.x}, Y: ${ocrBlock.y}")
                        }
                    }

                    // Menampilkan semua hasil OCR di Log untuk debugging
                    Log.d("OCR_RESULT", "Total Blok OCR: ${blocks.size}")

                    // Panggil parsing dengan data yang sudah dikumpulkan
                    parseKtpData(
                        blocks, nik, name,
                        address,
                    )
                }
                .addOnFailureListener { e ->
                    extractedText.value = "Error: ${e.message}"
                    Log.e("OCR_ERROR", "Gagal memproses OCR: ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    // Fungsi untuk meningkatkan kualitas dan resize gambar
    private fun enhanceAndResizeImage(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        // Rotasi gambar jika diperlukan
        val rotatedBitmap = if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }

        // Menyesuaikan ukuran dengan mempertahankan rasio aspek
        val aspectRatio = rotatedBitmap.width.toFloat() / rotatedBitmap.height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (rotatedBitmap.width > rotatedBitmap.height) { // Landscape
            targetHeight = 480
            targetWidth = (targetHeight * aspectRatio).toInt()
        } else { // Portrait
            targetWidth = 640
            targetHeight = (targetWidth / aspectRatio).toInt().coerceAtLeast(480)
        }

        val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, targetWidth, targetHeight, true)

        // Buat bitmap mutable untuk meningkatkan kontras & kecerahan tanpa crash
        val mutableBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Tingkatkan kontras dan kecerahan
        val contrast = 1.5f
        val brightness = 20f
        val colorMatrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,
                    0f, contrast, 0f, 0f, brightness,
                    0f, 0f, contrast, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }

        val paint = Paint()
        val canvas = Canvas(mutableBitmap)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(mutableBitmap, 0f, 0f, paint)

        return mutableBitmap
    }



    data class OcrBlock(val text: String, val x: Float, val y: Float)

    private fun parseKtpData(
        blocks: List<OcrBlock>,
        nik: MutableState<String>,
        name: MutableState<String>,
        address: MutableState<String>
    ) {
        // Daftar kemungkinan variasi label berdasarkan hasil OCR
        val nikLabels = listOf("nik", "nik:")
        val nameLabels = listOf("nama", "nama:", "nema:", "aama",  "nema", "name", "name:")
        val addressLabels = listOf("alamat", "alamat:", "lamat", "lamat:", "alaahom", "alaahom:", "alama", "alama:", "alamao", "alamao:", "alamarw", "alamarw:")
        val rtRwLabels = listOf("rt/rw", "rt/rw:", "rw ", "rw:", "rt ", "rt:", "rtrw", "rtrw:", "rtirw ", "rtirw:", "rtaw:", "rtaw")

        var foundName = false
        var foundNik = false
        var foundAddress = false
        var foundRtRw = false
        var tempAddress = ""
        var tempRtRw = ""

        fun cleanText(text: String): String {
            return text.trim().removePrefix(":").removePrefix("*").trim()
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





    // Utility untuk konversi ImageProxy ke Bitmap
    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}

@Composable
fun TestKtpOcr() {
    MLKitKtpScanner()
}


