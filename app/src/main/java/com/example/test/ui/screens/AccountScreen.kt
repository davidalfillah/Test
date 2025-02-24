package com.example.test.ui.screens

import android.util.Log
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.test.AuthViewModel
import com.example.test.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(authViewModel: AuthViewModel, paddingValues: PaddingValues, onRegisterClick: () -> Unit, onLoginClick: () -> Unit, onProfileClick: () -> Unit) {
    val user by authViewModel.user.collectAsState()
    Log.d("user", user.toString())
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Akun Saya") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = { // Tambahkan aksi di kanan atas
                    IconButton(onClick = { /* Aksi ketika tombol notifikasi ditekan */ }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_qr_code_scanner_24), // Ikon lonceng
                            contentDescription = "QR Anggota",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
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
            // 🔹 Header berbentuk kotak
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                ) {

                    if (user != null) {
                        // ✅ Jika sudah login, tampilkan info profil
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = user?.profilePicUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                                        contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = user?.name ?: "User", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.surfaceContainer)
                                Text(text = user?.uid ?: "User", fontSize = 14.sp, color = MaterialTheme.colorScheme.surfaceContainer)
                                Text(text = "Lihat Profil", fontSize = 14.sp, color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.clickable { onProfileClick() })
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally // Mengatur agar kontennya di tengah
                        ) {
                            Text(
                                text = "Anda belum masuk",
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )

                            Text(
                                text = "Silahkan masuk atau daftar.",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(onClick = onLoginClick) {
                                    Text(text = "Login")
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Button(onClick = onRegisterClick) {
                                    Text(text = "Register")
                                }
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth().padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))

            )
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth().padding(16.dp),

                    ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground), // Ganti dengan foto profil
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(fontSize = 11.sp, lineHeight = 13.sp, text = "GRIB atau Gerakan Rakyat Indonesia Bersatu didirikan oleh Rosario de Marshal, atau populer sebagai Hercules. Hercules adalah mantan gangster dan broker politik asal Timor Timur")
                        Text(
                            text = "Liat selengkapnya",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black, // Sesuaikan warna teks
                            modifier = Modifier.padding(top = 4.dp) // Jarak antara ikon dan teks
                        )
                    }
                }
            }
            Divider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp,
            )

            // 🔹 ListView (selalu muncul)
            SettingsList()

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 Tombol Logout (hanya muncul jika sudah login)
            if (user != null) {
                Row(modifier = Modifier.padding(horizontal = 8.dp).padding(bottom = 30.dp)){
                    OutlinedButton(
                        onClick = { authViewModel.logout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp), // Atur tinggi agar lebih proporsional
                        shape = RoundedCornerShape(8.dp), // Tidak terlalu bulat
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary) // Outline sesuai tema
                    ) {
                        Text(text = "Logout", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}




@Composable
fun SettingsList() {
    Column {
        ListItem(text = "Iuran Anggota", subText = "", icon = Icons.Default.DateRange)
        ListItem(text = "Riwayat Transaksi", subText = "", icon = ImageVector.vectorResource(R.drawable.baseline_history_24))
        ListItem(text = "Kartu Digital", subText = "Cetak Kartu", icon = ImageVector.vectorResource(R.drawable.baseline_card_membership_24))
        ListItem(text = "UMKM", subText = "Gabung UMKM", icon = ImageVector.vectorResource(R.drawable.baseline_storefront_24))
        ListItem(text = "Ajak Teman", subText = "", icon = ImageVector.vectorResource(R.drawable.baseline_supervised_user_circle_24))
        ListItem(text = "Pengaturan", subText = "", icon = Icons.Default.Settings)
        ListItem(text = "Tentang Hello GRIB", subText = "", icon = ImageVector.vectorResource(R.drawable.baseline_info_outline_24))
        ListItem(text = "Pusat Bantuan", subText = "", icon = Icons.Default.Call)
        ListItem(text = "Syarat & Ketentuan", subText = "", icon = ImageVector.vectorResource(R.drawable.baseline_newspaper_24))
        ListItem(text = "Kebijakan Privasi", subText = "", icon = ImageVector.vectorResource(R.drawable.baseline_privacy_tip_24))
    }
}

@Composable
fun ListItem(text: String, subText: String = "", icon: ImageVector) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Aksi jika diklik */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =  Arrangement.SpaceBetween
    ) {
        Row {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = subText, modifier = Modifier, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.baseline_arrow_forward_ios_24),
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}