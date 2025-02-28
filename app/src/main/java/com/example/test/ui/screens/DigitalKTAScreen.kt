package com.example.test.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.example.test.ui.components.UserProfileImage
import com.example.test.ui.dataType.Branch
import com.example.test.ui.dataType.Member
import com.example.test.ui.viewModels.MemberViewModel

@Composable
fun DigitalKTAScreen(userId: String, onBack: () -> Unit, memberViewModel: MemberViewModel = viewModel()) {
    var isLoading by remember { mutableStateOf(true) }
    var member by remember { mutableStateOf<Member?>(null) }
    var branch by remember { mutableStateOf<Branch?>(null) }

    // 🔹 Ambil data saat layar dimuat
    LaunchedEffect(userId) {
        memberViewModel.fetchMember(userId) { fetchedMember, fetchedBranch ->
            member = fetchedMember
            branch = fetchedBranch
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "KTA Digital", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (member != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    UserProfileImage(
                        "https://img.inews.co.id/media/600/files/networks/2024/10/10/ca3ea_ilustrasi-ktp-di-pinjem-pinjol-doc-istimewa.jpg",
                        100
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = member!!.fullName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "NIK: ${member!!.nik}", fontSize = 14.sp)
                    Text(text = "Tanggal Lahir: ${member!!.birthDate}", fontSize = 14.sp)
                    Text(text = "Cabang: ${branch?.name ?: "Tidak Diketahui"}", fontSize = 14.sp)
                    Text(text = "Level: ${branch?.level ?: "Tidak Diketahui"}", fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { onBack() }) {
                        Text("Kembali")
                    }
                }
            }
        } else {
            Text(text = "Data tidak ditemukan", color = Color.Red)
        }
    }
}

