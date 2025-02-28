package com.example.test.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.example.test.AuthViewModel
import com.example.test.ui.components.AddressInputField
import com.example.test.ui.components.CustomDropdown
import com.example.test.ui.components.CustomInputField
import com.example.test.ui.components.InputType
import com.example.test.ui.components.SlideComponentBanner
import com.example.test.ui.components.SlideComponentNews
import com.example.test.ui.dataTest.NewsData
import com.example.test.ui.dataTest.banners
import com.example.test.ui.dataTest.cities
import com.example.test.ui.dataTest.provinces
import com.example.test.ui.dataTest.subDistricts
import com.example.test.ui.dataTest.villages
import com.example.test.ui.viewModels.MemberViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    paddingValues: PaddingValues,
    navController: NavHostController,
    memberViewModel: MemberViewModel = viewModel(),
    authViewModel: AuthViewModel
) {
    val user by authViewModel.user.collectAsState()
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var nik by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var religion by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("Anggota") }
    var isRegistering by remember { mutableStateOf(false) }

    var selectedProvince by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("") }
    var selectedSubDistrict by remember { mutableStateOf("") }
    var selectedVillage by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val getImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it  // Simpan URI gambar

            // Proses gambar dengan ML Kit
            processImage(context, it) { extractedNik, extractedName, extractedAddress ->
                nik = extractedNik
                fullName = extractedName
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrasi Anggota") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                        .clickable { getImage.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "KTP Image",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = "Upload KTP", fontSize = 14.sp)
                    }
                }

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Nama Lengkap",
                    onValueChange = { fullName = it },
                    placeholder = "Masukkan nama lengkap Anda"
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "NIK",
                    onValueChange = { nik = it },
                    placeholder = "Masukkan NIK Anda"
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Tanggal Lahir",
                    onValueChange = { birthDate = it },
                    placeholder = "Masukkan tanggal lahir Anda"
                )

                CustomDropdown(
                    label = "Jenis Kelamin",
                    options = listOf("Laki-laki", "Perempuan"),
                    selectedValue = gender,
                    onSelected =  { gender = it }
                )

                AddressInputField(
                    selectedProvince = selectedProvince,
                    selectedCity = selectedCity,
                    selectedSubDistrict = selectedSubDistrict,
                    selectedVillage = selectedVillage,
                    onProvinceSelected = { selectedProvince = it },
                    onCitySelected = { selectedCity = it },
                    onSubDistrictSelected = { selectedSubDistrict = it },
                    onVillageSelected = { selectedVillage = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isRegistering = true
                        user?.let {
                            memberViewModel.registerMember(
                                userId = it.uid,
                                fullName = fullName,
                                nik = nik,
                                birthDate = birthDate,
                                gender = gender,
                                religion = religion,
                                education = education,
                                phone = phone,
                                street = street,
                                village = selectedVillage,
                                subDistrict = selectedSubDistrict,
                                city = selectedCity,
                                province = selectedProvince,  // ✅ Gunakan selectedProvince
                                postalCode = postalCode,
                                jobTitle = jobTitle
                            ) { success, message ->
                                isRegistering = false
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isRegistering
                ) {
                    Text(if (isRegistering) "Mendaftar..." else "Daftar")
                }
            }
        }
    }
}



// Proses Gambar dengan ML Kit
private fun processImage(
    context: Context,
    imageUri: Uri,
    onExtracted: (String, String, String) -> Unit
     ) {
    try {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromFilePath(context, imageUri)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedNik = extractNik(visionText.text)
                val extractedName = extractName(visionText.text)
                val extractedAddress = extractAddress(visionText.text)

                Log.d("MLKit", "NIK: $extractedNik, Nama: $extractedName, Alamat: $extractedAddress")

                onExtracted(extractedNik, extractedName, extractedAddress)
            }
            .addOnFailureListener { e ->
                Log.e("MLKit", "Gagal mengenali teks", e)
            }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

// Ekstraksi Data dari Hasil OCR
private fun extractNik(text: String): String {
    val nikRegex = Regex("\\b\\d{16}\\b") // Mendeteksi 16 digit angka
    return nikRegex.find(text)?.value ?: "NIK tidak ditemukan"
}

private fun extractName(text: String): String {
    val nameRegex = Regex("Nama\\s*:\\s*(.*)")
    return nameRegex.find(text)?.groups?.get(1)?.value ?: "Nama tidak ditemukan"
}

private fun extractAddress(text: String): String {
    val addressRegex = Regex("Alamat\\s*:\\s*(.*)")
    return addressRegex.find(text)?.groups?.get(1)?.value ?: "Alamat tidak ditemukan"
}

