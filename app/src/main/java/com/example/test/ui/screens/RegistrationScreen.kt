package com.example.test.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.test.ui.viewModels.MemberViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    paddingValues: PaddingValues,
    navController: NavHostController,
    memberViewModel: MemberViewModel = viewModel(),
    authViewModel: AuthViewModel,
) {
    val user by authViewModel.user.collectAsState()
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    var ktpUri by remember { mutableStateOf<Uri?>(null) }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var nik by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf<Timestamp?>(null) }
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

    var ktpError by remember { mutableStateOf("") }
    var fotoError by remember { mutableStateOf("") }
    var nikError by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    var birthDateError by remember { mutableStateOf("") }
    var genderError by remember { mutableStateOf("") }
    var religionError by remember { mutableStateOf("") }
    var educationError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var jobError by remember { mutableStateOf("") }
    var postalCodeError by remember { mutableStateOf("") }
    var addressError by remember { mutableStateOf("") }
    var provinceError by remember { mutableStateOf("") }
    var cityError by remember { mutableStateOf("") }
    var subDistrictError by remember { mutableStateOf("") }
    var villageError by remember { mutableStateOf("") }

    val isButtonEnabled = agree.isNotEmpty() && !isRegistering && isAllFieldsValid(
        nikError,
        nameError,
        birthDateError,
        genderError,
        religionError,
        educationError,
        phoneError,
        jobError,
        addressError,
        provinceError,
        cityError,
        subDistrictError,
        villageError,
        postalCodeError,
        fotoError,
        ktpError
    )

    val launcherKTP = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            ktpUri = uri
            ktpError = ""
        }
    }

    val launcherFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoUri = uri
            fotoError = ""
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
                Text(
                    "Upload Foto KTP",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (ktpError.isNotEmpty()) Color.Red else MaterialTheme.colorScheme.outline
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "Tata Cara Upload KTP:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Pastikan KTP berada dalam kondisi terang dan jelas.",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "2. Foto KTP secara penuh dan tidak terpotong.",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "3. Hindari pantulan cahaya dan bayangan.",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ktpUri?.let {
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
                                    onClick = { ktpUri = null },
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

                        if (ktpUri == null) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(onClick = { launcherKTP.launch("image/*") }) {
                                Text(text = "Pilih dari Galeri")
                            }
                        }

                    }
                }
                if (ktpError.isNotEmpty()) {
                    Text(
                        text = ktpError,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                CustomInputField(
                    type = InputType.TEXT,
                    label = "NIK",
                    placeholder = "Masukkan NIK",
                    selectedOption = nik,
                    onValueChange = {
                        nik = it
                        nikError = if (it.isEmpty()) "NIK tidak boleh kosong!" else ""
                    },
                    errorMessage = nikError,
                    required = true
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Nama Lengkap",
                    placeholder = "Masukkan Nama",
                    selectedOption = fullName,
                    onValueChange = {
                        fullName = it
                        nameError = if (it.isEmpty()) "Nama Lengkap tidak boleh kosong!" else ""
                    },
                    errorMessage = nameError,
                    required = true
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Jabatan",
                    placeholder = "Jabatan",
                    selectedOption = jobTitle,
                    onValueChange = { },
                    required = true
                )

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            ),
                            shape = MaterialTheme.shapes.medium
                        ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Secara default anda terdaftar sebagai anggota",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 13.sp,
                                text = "Jika anda Pengurus DPP/DPC/PAC atau pengurus lainnya anda bisa mengajukan diri melalui menu KTA"
                            )
                        }
                    }
                }

                CustomInputField(
                    type = InputType.DATE,
                    label = "Tanggal Lahir",
                    onDateValueChange = { timestamp ->
                        birthDate = timestamp
                        birthDateError = ""
                    },
                    selectedOption = birthDate?.let {
                        val date = Date(it.seconds * 1000)
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                    } ?: "",
                    errorMessage = birthDateError,
                    placeholder = "Masukkan tanggal lahir Anda",
                    required = true
                )


                CustomDropdown(
                    label = "Jenis Kelamin",
                    required = true,
                    options = listOf("Laki-laki", "Perempuan"),
                    selectedValue = gender,
                    errorMessage = genderError,
                    onSelected = {
                        gender = it
                        genderError = if (it.isEmpty()) "Jenis Kelamin tidak boleh kosong!" else ""
                    }
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Agama",
                    placeholder = "Masukkan Agama",
                    selectedOption = religion,
                    errorMessage = religionError,
                    onValueChange = {
                        religion = it
                        religionError = if (it.isEmpty()) "Agama tidak boleh kosong!" else ""
                    },
                    required = true
                )

                CustomDropdown(
                    label = "Pendidikan Terakhir",
                    required = true,
                    options = listOf("SD", "SMP", "SMA", "S1", "S2", "S3"),
                    selectedValue = education,
                    errorMessage = educationError,
                    onSelected = {
                        education = it
                        educationError = if (it.isEmpty()) "Pendidikan tidak boleh kosong!" else ""
                    }
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Nomor Telepon",
                    placeholder = "Masukkan Nomor Telepon",
                    selectedOption = phone,
                    errorMessage = phoneError,
                    onValueChange = {
                        phone = it
                        phoneError = if (it.isEmpty()) "Nomor Telepon tidak boleh kosong!" else ""
                    },
                    required = true
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Pekerjaan",
                    placeholder = "Masukkan Pekerjaan",
                    selectedOption = job,
                    onValueChange = {
                        job = it
                        jobError = if (it.isEmpty()) "Pekerjaan tidak boleh kosong!" else ""
                                    },
                    required = true,
                    errorMessage = jobError
                )



                CustomInputField(
                    type = InputType.TEXT,
                    description = "Keanggotaan anda terdaftar sesuai dengan Alamat, Kelurahan/Desa, Kecamatan, Kabupaten/Kota, dan Provinsi yang anda masukan. pastikan alamat anda sesuai dengan alamat pendaftaran anda.",
                    label = "Alamat",
                    placeholder = "Masukkan Alamat",
                    selectedOption = street,
                    errorMessage = addressError,
                    onValueChange = {
                        street = it
                        addressError = if (it.isEmpty()) "Alamat tidak boleh kosong!" else ""
                    },
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
                    errorMessage = postalCodeError,
                    onValueChange = {
                        postalCode = it
                        postalCodeError = if (it.isEmpty()) "Kode Pos tidak boleh kosong!" else ""
                    },
                    required = true
                )

                Text(
                    "Upload Foto Anggota",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    if (fotoUri == null) {
                        Card(
                            modifier = Modifier
                                .width(120.dp) // Lebar pas foto
                                .height(160.dp) // Tinggi pas foto
                                .clickable { launcherFoto.launch("image/*") },
                            border = BorderStroke(
                                1.dp,
                                if (fotoError.isNotEmpty()) Color.Red else MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Pilih Foto KTA")
                            }
                        }
                    } else {
                        fotoUri?.let {
                            Box(
                                modifier = Modifier
                                    .width(120.dp) // Lebar pas foto
                                    .height(160.dp) // Tinggi pas foto
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        if (fotoError.isNotEmpty()) Color.Red else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                            )
                            {
                                Image(
                                    painter = rememberAsyncImagePainter(it),
                                    contentDescription = "Foto KTA",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Tombol Hapus (Icon X) di pojok kanan atas
                                IconButton(
                                    onClick = { fotoUri = null },
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
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "1. Foto ini akan di pakai untuk KTA",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "2. Pastikan foto ini jelas dan tidak terpotong",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "3. Foto ini harus berisi wajah anggota",
                            style = MaterialTheme.typography.labelLarge
                        )

                        if (fotoError.isNotEmpty()) {
                            Text(
                                text = fotoError,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
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
                    showLabel = false,
                    selectedOption = agree,
                    onValueChange = { if (agree.isEmpty()) agree = it else agree = "" },
                )


                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Cek apakah sudah dalam proses registrasi
                        if (isRegistering) return@Button

                        // Mengaktifkan indikator proses registrasi
                        isRegistering = true

                        // Validasi field
                        nikError = if (nik.isEmpty()) "NIK tidak boleh kosong!" else ""
                        nameError =
                            if (fullName.isEmpty()) "Nama Lengkap tidak boleh kosong!" else ""
                        birthDateError =
                            if (birthDate == null) "Tanggal Lahir tidak boleh kosong!" else ""
                        genderError =
                            if (gender.isEmpty()) "Jenis Kelamin tidak boleh kosong!" else ""
                        religionError = if (religion.isEmpty()) "Agama tidak boleh kosong!" else ""
                        educationError =
                            if (education.isEmpty()) "Pendidikan tidak boleh kosong!" else ""
                        phoneError =
                            if (phone.isEmpty()) "Nomor Telepon tidak boleh kosong!" else ""
                        jobError = if (job.isEmpty()) "Pekerjaan tidak boleh kosong!" else ""
                        addressError = if (street.isEmpty()) "Alamat tidak boleh kosong!" else ""
                        provinceError =
                            if (selectedProvince.isEmpty()) "Provinsi tidak boleh kosong!" else ""
                        cityError =
                            if (selectedCity.isEmpty()) "Kota/Kabupaten tidak boleh kosong!" else ""
                        subDistrictError =
                            if (selectedSubDistrict.isEmpty()) "Kecamatan tidak boleh kosong!" else ""
                        villageError =
                            if (selectedVillage.isEmpty()) "Kelurahan tidak boleh kosong!" else ""
                        postalCodeError =
                            if (postalCode.isEmpty()) "Kode Pos tidak boleh kosong!" else ""
                        fotoError = if (fotoUri == null) "Foto Anggota tidak boleh kosong!" else ""
                        ktpError = if (ktpUri == null) "Foto KTP tidak boleh kosong!" else ""

                        // Jika ada error, hentikan proses registrasi
                        if (nikError.isNotEmpty() || nameError.isNotEmpty() || birthDateError.isNotEmpty() ||
                            genderError.isNotEmpty() || religionError.isNotEmpty() || educationError.isNotEmpty() ||
                            phoneError.isNotEmpty() || jobError.isNotEmpty() || addressError.isNotEmpty() ||
                            provinceError.isNotEmpty() || cityError.isNotEmpty() || subDistrictError.isNotEmpty() ||
                            villageError.isNotEmpty() || postalCodeError.isNotEmpty() ||
                            fotoError.isNotEmpty() || ktpError.isNotEmpty()
                        ) {
                            isRegistering = false
                            return@Button
                        }

                        // Proses pendaftaran jika semua validasi lolos
                        user?.let {
                            fotoUri?.let { it1 ->
                                ktpUri?.let { it2 ->
                                    birthDate?.let { it3 ->
                                        memberViewModel.registerMember(
                                            fotoUri = it1,
                                            ktpUri = it2,
                                            userId = it.uid,
                                            fullName = fullName,
                                            nik = nik,
                                            birthDate = it3,
                                            gender = gender,
                                            religion = religion,
                                            education = education,
                                            phone = phone,
                                            street = street,
                                            job = job,
                                            village = selectedVillage,
                                            subDistrict = selectedSubDistrict,
                                            city = selectedCity,
                                            province = selectedProvince,
                                            postalCode = postalCode,
                                            jobTitle = jobTitle,
                                            context = context
                                        ) { success, message ->
                                            // Proses selesai, nonaktifkan loading
                                            isRegistering = false
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
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

fun isAllFieldsValid(
    nikError: String,
    nameError: String,
    birthDateError: String,
    genderError: String,
    religionError: String,
    educationError: String,
    phoneError: String,
    jobError: String,
    addressError: String,
    provinceError: String,
    cityError: String,
    subDistrictError: String,
    villageError: String,
    postalCodeError: String,
    fotoError: String,
    ktpError: String
): Boolean {
    return nikError.isEmpty() && nameError.isEmpty() && birthDateError.isEmpty() &&
            genderError.isEmpty() && religionError.isEmpty() && educationError.isEmpty() &&
            phoneError.isEmpty() && jobError.isEmpty() && addressError.isEmpty() &&
            provinceError.isEmpty() && cityError.isEmpty() && subDistrictError.isEmpty() &&
            villageError.isEmpty() && postalCodeError.isEmpty() &&
            fotoError.isEmpty() && ktpError.isEmpty()
}


















