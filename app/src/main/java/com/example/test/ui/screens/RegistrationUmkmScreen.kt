package com.example.test.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.example.test.AuthViewModel
import com.example.test.ui.components.AddressInputField
import com.example.test.ui.components.CustomDropdown
import com.example.test.ui.components.CustomInputField
import com.example.test.ui.components.InputType
import com.example.test.ui.viewModels.MemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationUmkmScreen(paddingValues: PaddingValues, navController: NavHostController, memberViewModel: MemberViewModel = viewModel(), authViewModel: AuthViewModel) {
    val user by authViewModel.user.collectAsState()

    val context = LocalContext.current

    var umkmName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var selectedProvince by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("") }
    var selectedSubDistrict by remember { mutableStateOf("") }
    var selectedVillage by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var umkmUri by remember { mutableStateOf<Uri?>(null) }
    var isRegistering by remember { mutableStateOf(false) }

    var umkmNameError by remember { mutableStateOf("") }
    var businessTypeError by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf("") }
    var streetError by remember { mutableStateOf("") }
    var provinceError by remember { mutableStateOf("") }
    var cityError by remember { mutableStateOf("") }
    var subDistrictError by remember { mutableStateOf("") }
    var villageError by remember { mutableStateOf("") }
    var postalCodeError by remember { mutableStateOf("") }
    var contactError by remember { mutableStateOf("") }
    var umkmUriError by remember { mutableStateOf("") }

    val launcherUmkm = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            umkmUri = uri
            umkmUriError = ""
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrasi UMKM") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, // Ikon panah kembali
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
                .padding(top = innerPadding.calculateTopPadding(), // Jaga jarak dari AppBar
                    bottom = paddingValues.calculateBottomPadding()) // Hindari tumpang tindih BottomNav)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                CustomInputField(
                    type = InputType.TEXT,
                    label = "Nama UMKM",
                    placeholder = "Masukkan Nama UMKM",
                    selectedOption = umkmName,
                    onValueChange = {
                        umkmName = it
                        umkmNameError = if (it.isEmpty()) "Nama UMKM tidak boleh kosong!" else ""
                    },
                    errorMessage = umkmNameError,
                    required = true
                )
                CustomDropdown(
                    label = "Jenis Usaha",
                    required = true,
                    options = listOf("Industri Kecil", "Industri Menengah", "Industri Besar"),
                    selectedValue = businessType,
                    errorMessage = businessTypeError,
                    onSelected = {
                        businessType = it
                        businessTypeError = if (it.isEmpty()) "Jenis Usaha tidak boleh kosong!" else ""
                    }
                )
                CustomInputField(
                    type = InputType.TEXT,
                    label = "Deskripsi Usaha",
                    placeholder = "Masukkan Deskripsi Usaha",
                    selectedOption = description,
                    onValueChange = {
                        description = it
                        descriptionError = if (it.isEmpty()) "Deskripsi tidak boleh kosong!" else ""
                    },
                    errorMessage = descriptionError,
                    required = true
                )
                CustomInputField(
                    type = InputType.TEXT,
                    label = "Alamat",
                    placeholder = "Masukkan Alamat",
                    selectedOption = street,
                    onValueChange = {
                        street = it
                        streetError = if (it.isEmpty()) "Alamat tidak boleh kosong!" else ""
                    },
                    errorMessage = streetError,
                    required = true
                )
                AddressInputField(
                    selectedProvince = selectedProvince,
                    selectedCity = selectedCity,
                    selectedSubDistrict = selectedSubDistrict,
                    selectedVillage = selectedVillage,
                    onProvinceSelected = {
                        selectedProvince = it
                        provinceError = if (it.isEmpty()) "Provinsi tidak boleh kosong!" else ""
                    },
                    onCitySelected = {
                        selectedCity = it
                        cityError = if (it.isEmpty()) "Kota/Kabupaten tidak boleh kosong!" else ""
                    },
                    onSubDistrictSelected = {
                        selectedSubDistrict = it
                        subDistrictError = if (it.isEmpty()) "Kecamatan tidak boleh kosong!" else ""
                    },
                    onVillageSelected = {
                        selectedVillage = it
                        villageError =
                            if (it.isEmpty()) "Kelurahan/Desa tidak boleh kosong!" else ""
                    },
                    errorMessageProvince = provinceError,
                    errorMessageCity = cityError,
                    errorMessageSubDistrict = subDistrictError,
                    errorMessageVillage = villageError
                )
                CustomInputField(
                    type = InputType.TEXT,
                    label = "Kode Pos",
                    placeholder = "Masukkan Kode Pos",
                    selectedOption = postalCode,
                    onValueChange = {
                        postalCode = it
                        postalCodeError = if (it.isEmpty()) "Kode Pos tidak boleh kosong!" else ""
                    },
                    errorMessage = postalCodeError,
                    required = true
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Nomor Telepon",
                    placeholder = "Masukkan Nomor Telepon",
                    selectedOption = contact,
                    onValueChange = {
                        contact = it
                        contactError = if (it.isEmpty()) "Kontak UMKM tidak boleh kosong!" else ""
                    },
                    errorMessage = contactError,
                    required = true
                )

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (umkmUriError.isNotEmpty()) Color.Red else MaterialTheme.colorScheme.outline
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "Unggah Foto Tempat Usaha",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Foto tampak depan tempat usaha",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "2. Pemilik usaha harus terlihat jelas dalam foto.",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        umkmUri?.let {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 3f) // Rasio KTP 4:3
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(it),
                                    contentDescription = "KTP",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )

                                // Tombol Hapus (Icon X)
                                IconButton(
                                    onClick = { umkmUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .background(
                                            color = Color.White.copy(alpha = 0.8f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus Foto",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }

                        if (umkmUri == null) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(onClick = { launcherUmkm.launch("image/*") }) {
                                Text(text = "Pilih dari Galeri")
                            }
                        }

                    }
                }
                if (umkmUriError.isNotEmpty()) {
                    Text(
                        text = umkmUriError,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isRegistering = true
                        user?.let { currentUser ->
                            memberViewModel.getMemberByUserId(currentUser.uid) { member ->
                                if (member != null) {
                                    umkmUri?.let {
                                        memberViewModel.registerUmkm(
                                            context = context,
                                            memberImageUri = it,
                                            memberId = member.memberId,
                                            name = umkmName,
                                            businessType = businessType,
                                            description = description,
                                            street = street,
                                            village = selectedVillage,
                                            subDistrict = selectedSubDistrict,
                                            city = selectedCity,
                                            province = selectedProvince,
                                            postalCode = postalCode,
                                            contact = contact
                                        ) { success, message ->
                                            isRegistering = false
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            if (success) {
                                                navController.navigate("umkm") {
                                                    popUpTo("registrationUmkm") { inclusive = true }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    isRegistering = false
                                    Toast.makeText(context, "Anda belum terdaftar sebagai anggota!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isRegistering
                ) {
                    Text(if (isRegistering) "Mendaftar..." else "Daftar UMKM")
                }
            }

        }
    }

}