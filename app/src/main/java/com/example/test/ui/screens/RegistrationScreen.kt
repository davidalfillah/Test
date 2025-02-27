package com.example.test.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.test.AuthViewModel
import com.example.test.ui.components.SlideComponentBanner
import com.example.test.ui.components.SlideComponentNews
import com.example.test.ui.dataTest.NewsData
import com.example.test.ui.dataTest.banners
import com.example.test.ui.viewModels.MemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(paddingValues: PaddingValues, navController: NavHostController, memberViewModel: MemberViewModel = viewModel(), authViewModel: AuthViewModel) {
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
    var village by remember { mutableStateOf("") }
    var subDistrict by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("Anggota") }
    var isRegistering by remember { mutableStateOf(false) }


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
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = nik, onValueChange = { nik = it }, label = { Text("NIK") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Tanggal Lahir") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Jenis Kelamin") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = religion, onValueChange = { religion = it }, label = { Text("Agama") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = education, onValueChange = { education = it }, label = { Text("Pendidikan Terakhir") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Nomor HP") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Jalan") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("Desa/Kelurahan") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = subDistrict, onValueChange = { subDistrict = it }, label = { Text("Kecamatan") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Kabupaten/Kota") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = province, onValueChange = { province = it }, label = { Text("Provinsi") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = postalCode, onValueChange = { postalCode = it }, label = { Text("Kode Pos") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isRegistering = true
                        user?.let {
                            memberViewModel.registerMember(
                                userId = it.uid,  // Kirim userId ke fungsi
                                fullName = fullName,
                                nik = nik,
                                birthDate = birthDate,
                                gender = gender,
                                religion = religion,
                                education = education,
                                phone = phone,
                                street = street,
                                village = village,
                                subDistrict = subDistrict,
                                city = city,
                                province = province,
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
