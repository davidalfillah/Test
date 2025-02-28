package com.example.test.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    authViewModel: AuthViewModel,
    nik: String, fullName: String, street: String, imageUrl: String? = null, birthDate: String
) {
    val user by authViewModel.user.collectAsState()
    val context = LocalContext.current
    Log.d("BirthDate", birthDate)

    var gender by remember { mutableStateOf("") }
    var religion by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("Anggota") }
    var isRegistering by remember { mutableStateOf(false) }

    var selectedProvince by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("") }
    var selectedSubDistrict by remember { mutableStateOf("") }
    var selectedVillage by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf("") }


    var inputImageUri by remember { mutableStateOf(imageUrl) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val getPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { photoUri = it }
    }

    val isButtonEnabled = agree.isNotEmpty() && !isRegistering


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
                Text("Foto KTP", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (inputImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(inputImageUri),
                        contentDescription = "Foto KTP",
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                } else {
                    Text("Belum ada foto KTP", modifier = Modifier.padding(16.dp))
                }


                CustomInputField(
                    type = InputType.TEXT,
                    label = "NIK",
                    placeholder = "Masukkan NIK",
                    selectedOption = nik,
                    onValueChange = {  }
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Nama Lengkap",
                    placeholder = "Masukkan Nama",
                    selectedOption = fullName,
                    onValueChange = {  }
                )


                CustomInputField(
                    type = InputType.TEXT,
                    label = "Tanggal Lahir",
                    onValueChange = {  },
                    selectedOption = birthDate,
                    placeholder = "Masukkan tanggal lahir Anda"
                )

                CustomDropdown(
                    label = "Jenis Kelamin",
                    options = listOf("Laki-laki", "Perempuan"),
                    selectedValue = gender,
                    onSelected =  { gender = it }
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Agama",
                    placeholder = "Masukkan Agama",
                    selectedOption = religion,
                    onValueChange = { religion = it }
                )

                CustomDropdown(
                    label = "Pendidikan Terakhir",
                    options = listOf("SD", "SMP", "SMA", "S1", "S2", "S3"),
                    selectedValue = education,
                    onSelected =  { education = it }
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Nomor Telepon",
                    placeholder = "Masukkan Nomor Telepon",
                    selectedOption = phone,
                    onValueChange = { phone = it }
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Pekerjaan",
                    placeholder = "Masukkan Pekerjaan",
                    selectedOption = job,
                    onValueChange = { job = it }
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Alamat",
                    placeholder = "Masukkan Alamat",
                    selectedOption = street,
                    onValueChange = {  }
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

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Kode Pos",
                    placeholder = "Masukkan Kode Pos",
                    selectedOption = postalCode,
                    onValueChange = { postalCode = it }
                )


                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                        .clickable { getPhoto.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(photoUri),
                            contentDescription = "Foto Anggota",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = "Upload Foto Anggota", fontSize = 14.sp)
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Saya yang bertanda tangan dibawah ini menyatakan bahwa semua data yang saya sampaikan ini adalah benar, dalam keadaan sehat jasmani rohani dan tanpa paksaan dari pihak manapun.\n\n" +
                            "Saya bersedia mematuhi Anggaran Dasar dan Anggaran Rumah Tangga (AD/ART) Ormas GRIB JAYA, Peraturan organisasi serta keputusan-keputusan lainnya yang ditetapkan.\n\n" +
                            "Bersedia berpartisipasi aktif dalam susunan kepengurusan organisasi Gerakan Rakyat Indonesia Bersatu di tingkat manapun.\n\n" +
                            "Bersedia menjalankan tugas dan fungsi sesuai Visi Misi organisasi serta menjunjung tinggi nama baik organisasi dimanapun berada.\n\n" +
                            "Saya siap dikenakan sanksi apabila di kemudian hari saya terbukti melakukan hal-hal yang merugikan dan merusak nama baik organisasi GRIB JAYA.",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    color = Color.Black
                )

                CustomInputField(
                    type = InputType.CHECKBOX,
                    label = "Setuju",
                    showLable = false,
                    selectedOption = agree,
                    onValueChange = { if(agree.isEmpty()) agree = it else agree = "" },
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
                                job = job,
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
                    enabled = isButtonEnabled
                ) {
                    Text(if (isRegistering) "Mendaftar..." else "Daftar")
                }
            }
        }
    }
}



















