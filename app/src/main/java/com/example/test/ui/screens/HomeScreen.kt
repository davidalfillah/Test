package com.example.test.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.test.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.ViewModel
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import com.example.test.AuthViewModel
import com.example.test.ui.components.SlideComponentBanner
import com.example.test.ui.components.SlideComponentCharity
import com.example.test.ui.components.SlideComponentNews
import com.example.test.ui.components.SlideComponentProduct
import com.example.test.ui.components.UserProfileImage
import com.example.test.ui.dataTest.NewsData
import com.example.test.ui.dataTest.banners
import com.example.test.ui.dataTest.charitys
import com.example.test.ui.dataTest.products
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

data class Donation(
    val image: Int,       // ID Gambar dari drawable
    val title: String,    // Judul donasi
    val target: Int,      // Target donasi (contoh: Rp10.000.000)
    val collected: Int    // Donasi yang sudah terkumpul
)

data class Product(
    val image: String,       // ID Gambar dari drawable
    val title: String,    // Judul donasi
    val price: Int,      // Target donasi (contoh: Rp10.000.000)
)

data class User(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val role: String = "user",
    val profilePicUrl: String = "",
    var isProfileComplete: Boolean = false,
    val createdAt: Long = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, paddingValues: PaddingValues, authViewModel: AuthViewModel) {
    val user by authViewModel.user.collectAsState()
    val isProfileComplete by authViewModel.isProfileComplete.collectAsState()

    LaunchedEffect(user, isProfileComplete) {
        if (user != null && !isProfileComplete) {
            Log.d("Navigation", "Navigating to profile_setup")
            navController.navigate("profile_setup") {
                popUpTo("home") { inclusive = true }
            }
        }
    }




    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Icon(
                        painter = painterResource(id = R.drawable.grib_02), // Ganti dengan nama file vektor di drawable
                        contentDescription = "Logo",
                        modifier = Modifier.size(120.dp), // Ukuran ikon
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = { // Tambahkan aksi di kanan atas
                    IconButton(onClick = { /* Aksi ketika tombol notifikasi ditekan */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications, // Ikon lonceng
                            contentDescription = "Notifikasi",
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
        if(user != null){
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp).fillMaxWidth(), horizontalArrangement =  Arrangement.SpaceBetween) {
                            Column {
                                Text(text = "Iuran Anggota Bulanan", fontSize = 14.sp, color = Color.White)
                                Text(text = "Rp.10.000", fontWeight = FontWeight.Bold, fontSize = 36.sp)
                                Text(text = "Iuran bulan April - 2025", fontSize = 14.sp, color = Color.White)
                            }
                            Column {
                                Button(
                                    onClick = {  },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer,  // Warna latar belakang dark
                                        contentColor = MaterialTheme.colorScheme.primary // Warna teks tetap putih
                                    ),


                                    ) {
                                    Text(text = "Bayar")
                                }
                            }
                        }


                    }
                }
            }
        }
        Box(modifier = Modifier.height(77.dp).fillMaxWidth().background(color = MaterialTheme.colorScheme.primary))
        Column(modifier = Modifier.offset(y = (-75).dp)){
            Card(
                modifier = Modifier
                    .fillMaxWidth().padding(16.dp),

                )
            {
                Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                    if(user != null){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween // Perbaikan: Ubah dari SpaceBetween ke Start
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, false) // Perbaikan: Memberi ruang agar tombol tidak terdesak
                            ) {

                                UserProfileImage(user?.profilePicUrl, 42)

                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = user?.name ?: "User",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.wrapContentWidth() // Perbaikan: Pastikan teks tidak memakan seluruh ruang
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp)) // Perbaikan: Tambahkan jarak agar tidak terlalu rapat

                            Button(
                                onClick = { },
                                modifier = Modifier.wrapContentWidth() // Perbaikan: Pastikan tombol tidak dipaksa melebar
                            ) {
                                Text(
                                    text = "Ajak Teman",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.wrapContentWidth()
                                )
                            }
                        }


                    }else{
                        Row (verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp).fillMaxWidth(), horizontalArrangement =  Arrangement.SpaceBetween){
                            Text(text = "Anda Belum Login", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Button(
                                onClick = { navController.navigate("login") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                            ) {
                                Text(text = "Log In")
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val items = listOf(
                            "News" to R.drawable.baseline_newspaper_24,
                            "UMKM" to R.drawable.baseline_storefront_24,
                            "Gabung GRIB" to R.drawable.baseline_card_membership_24,
                            "Donasi" to R.drawable.baseline_wallet_24
                        )

                        items.forEachIndexed { index, (label, icon) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        when (index) {
                                            0 -> navController.navigate("news")
                                            2 -> navController.navigate("status")
                                            // Tambahkan navigasi sesuai kebutuhan
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp) // Ukuran tombol
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(2.dp), // Latar belakang bulat
                                    contentAlignment = Alignment.Center // Memastikan ikon di tengah
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = icon),
                                        contentDescription = label,
                                        modifier = Modifier.size(40.dp),
                                        tint = Color.White // Warna ikon agar kontras
                                    )
                                }
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth().padding(horizontal = 16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))

                )
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth().padding(16.dp),

                    ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = R.drawable.logo_grib),
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp)
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
            SlideComponentBanner(
                items = banners.shuffled().take(3),
                onItemClick = { menu ->
                    println("Menu yang diklik: $menu")
                },
                scrollInterval = 5000L
            )
            SlideComponentNews(
                items = NewsData.newsList,
                onItemClick = { menu ->
                        navController.navigate("news_detail/$menu")
                },
                navController = navController
            )
            SlideComponentCharity(
                items = charitys,
                onItemClick = { menu ->
                    println("Menu yang diklik: $menu")
                }
            )
            SlideComponentProduct(
                items = products,
                onItemClick = { menu ->
                    println("Menu yang diklik: $menu")
                }
            )
        }

    }
}
}
