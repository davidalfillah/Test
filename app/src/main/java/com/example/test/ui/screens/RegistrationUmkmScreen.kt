package com.example.test.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.test.AuthViewModel
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
    var village by remember { mutableStateOf("") }
    var subDistrict by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }


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
                OutlinedTextField(value = umkmName, onValueChange = { umkmName = it }, label = { Text("Nama UMKM") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = businessType, onValueChange = { businessType = it }, label = { Text("Jenis Usaha") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi Usaha") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Jalan") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("Desa/Kelurahan") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = subDistrict, onValueChange = { subDistrict = it }, label = { Text("Kecamatan") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Kabupaten/Kota") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = province, onValueChange = { province = it }, label = { Text("Provinsi") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = postalCode, onValueChange = { postalCode = it }, label = { Text("Kode Pos") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Kontak UMKM") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isRegistering = true
                        user?.let { currentUser ->
                            memberViewModel.getMemberByUserId(currentUser.uid) { member ->
                                if (member != null) {
                                    memberViewModel.registerUmkm(
                                        memberId = member.memberId,
                                        name = umkmName,
                                        businessType = businessType,
                                        description = description,
                                        street = street,
                                        village = village,
                                        subDistrict = subDistrict,
                                        city = city,
                                        province = province,
                                        postalCode = postalCode,
                                        contact = contact
                                    ) { success, message ->
                                        isRegistering = false
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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