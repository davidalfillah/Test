package com.example.test.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.test.ui.dataType.Branch
import com.example.test.ui.dataType.Member
import com.example.test.ui.viewModels.MemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberProfileScreen(navController: NavController, userId: String, onBack: () -> Unit, memberViewModel: MemberViewModel = MemberViewModel()) {
    val context = LocalContext.current
    var member by remember { mutableStateOf<Member?>(null) }
    var branch by remember { mutableStateOf<Branch?>(null) }

    // Ambil data member
    LaunchedEffect(userId) {
        memberViewModel.fetchMember(userId) { fetchedMember, fetchedBranch ->
            member = fetchedMember
            branch = fetchedBranch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Biodata Member") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            member?.let {
                Text("ID Anggota: ${it.memberId}", fontWeight = FontWeight.Bold)
                Text("Nama: ${it.fullName}")
                Text("NIK: ${it.nik}")
                Text("Tanggal Lahir: ${it.birthDate}")
                Text("Jenis Kelamin: ${it.gender}")
                Text("Agama: ${it.religion}")
                Text("Pendidikan: ${it.education}")
                Text("Pekerjaan: ${it.job}")
                Text("Alamat: ${it.address.street}, ${it.address.village}, ${it.address.subDistrict}, ${it.address.city}, ${it.address.province}")
                Text("Kode Pos: ${it.address.postalCode}")
                Text("Jabatan: ${it.jobTitle}")
                branch?.let { b -> Text("Cabang: ${b.name}") }

                Spacer(modifier = Modifier.height(16.dp))

//                // Tombol untuk melihat KTP
//                Button(
//                    onClick = {
//                        it.ktpUrl?.let { url ->
//                            navController.navigate("viewKtp?url=$url")
//                        } ?: Toast.makeText(context, "KTP tidak tersedia", Toast.LENGTH_SHORT).show()
//                    }
//                ) {
//                    Text("Lihat KTP")
//                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol untuk melihat KTA Digital
                Button(
                    onClick = {
                        navController.navigate("kta/${it.userId}")
                    }
                ) {
                    Text("Lihat KTA Digital")
                }
            } ?: run {
                Text("Memuat data...", textAlign = TextAlign.Center)
            }
        }
    }
}
