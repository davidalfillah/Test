package com.example.test.ui.screens

import android.util.Log
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.example.test.AuthViewModel
import com.example.test.R
import com.example.test.ui.components.PublicComplaints
import com.example.test.ui.components.ShareBottomSheet
import com.example.test.ui.components.SlideComponentBanner
import com.example.test.ui.components.SlideComponentNews
import com.example.test.ui.components.SlideComponentProduct
import com.example.test.ui.components.UserProfileImage
import com.example.test.ui.dataTest.NewsData
import com.example.test.ui.dataTest.products
import com.example.test.ui.viewModels.Ad
import com.example.test.ui.viewModels.AdViewModel
import com.google.firebase.Timestamp


data class Product(
    val image: String,       // ID Gambar dari drawable
    val title: String,    // Judul donasi
    val price: Int,      // Target donasi (contoh: Rp10.000.000)
)

data class User(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val lastSeen: Timestamp? = Timestamp.now(),
    val isOnline: Boolean = false,
    val role: String = "user",
    val profilePicUrl: String = "",
    var isProfileComplete: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController, paddingValues: PaddingValues, authViewModel: AuthViewModel
) {
    val ads = remember { mutableStateOf(emptyList<Ad>()) }
    val isLoading = remember { mutableStateOf(true) } // Status loading
    val user by authViewModel.user.collectAsState()
    val isProfileComplete by authViewModel.isProfileComplete.collectAsState()
    val adViewModel = AdViewModel()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(user, isProfileComplete) {
        if (user != null && !isProfileComplete) {
            Log.d("Navigation", "Navigating to profile_setup")
            navController.navigate("profile_setup") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        adViewModel.getAds { fetchedAds ->
            ads.value = fetchedAds
            isLoading.value = false // Matikan loading setelah data diambil
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Icon(
                painter = painterResource(id = R.drawable.grib_02), // Ganti dengan nama file vektor di drawable
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp), // Ukuran ikon
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ), actions = { // Tambahkan aksi di kanan atas
            IconButton(onClick = { /* Aksi ketika tombol notifikasi ditekan */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications, // Ikon lonceng
                    contentDescription = "Notifikasi",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(), // Jaga jarak dari AppBar
                    bottom = paddingValues.calculateBottomPadding()
                ) // Hindari tumpang tindih BottomNav)
                .verticalScroll(rememberScrollState())
        ) {
            if (user != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(
                                        1f, false
                                    ) // Perbaikan: Memberi ruang agar tombol tidak terdesak
                                ) {

                                    UserProfileImage(user?.profilePicUrl, 64)

                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Hai, ${user?.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.wrapContentWidth() // Perbaikan: Pastikan teks tidak memakan seluruh ruang
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp)) // Perbaikan: Tambahkan jarak agar tidak terlalu rapat

                                Button(
                                    onClick = {
                                        showBottomSheet = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer,  // Warna latar belakang dark
                                        contentColor = MaterialTheme.colorScheme.primary // Warna teks tetap putih
                                    ),
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


                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Anda Belum Masuk",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Silaklan masuk untuk melanjutkan", fontSize = 12.sp
                                    )
                                }
                                Button(
                                    onClick = { navController.navigate("login") },

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer,  // Warna latar belakang dark
                                        contentColor = MaterialTheme.colorScheme.primary // Warna teks tetap putih
                                    ),
                                ) {
                                    Text(text = "Log In")
                                }
                            }


                        }
                    }
                }

            }
            Box(
                modifier = Modifier
                    .height(77.dp)
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary)
            )
            Column(modifier = Modifier.offset(y = (-75).dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    shape = RoundedCornerShape(16.dp),
                    )

                {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        if (user != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween // Perbaikan: Ubah dari SpaceBetween ke Start
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically,) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = R.drawable._dicons_dollar_dynamic_color),
                                        contentDescription = "Koin",
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Rp.10.000",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 24.sp,
                                            lineHeight = 28.sp,
                                            color = Color.Black,
                                        )
                                        Text(
                                            text = "Iuran bulan April - 2025",
                                            fontSize = 12.sp,
                                            lineHeight = 14.sp,
                                            color = Color.Black.copy(alpha = 0.5f),
                                        )
                                    }
                                }
                                Column {
                                    Button(
                                        onClick = { },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        ),

                                        ) {
                                        Text(text = "Bayar")
                                    }
                                }

                            }

                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val items = listOf(
                                "News" to R.drawable._dicons_calender_dynamic_color,
                                "UMKM" to R.drawable._dicons_puzzle_dynamic_color,
                                "KTA" to R.drawable._dicons_medal_dynamic_color,
                                "Donasi" to R.drawable._dicons_gift_dynamic_color
                            )

                            items.forEachIndexed { index, (label, icon) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            when (index) {
                                                0 -> navController.navigate("news")
                                                1 -> navController.navigate("registerUmkm")
                                                2 -> user?.let {
                                                    authViewModel.checkMemberStatus(it.uid) { isMember ->
                                                        if (isMember) {
                                                            navController.navigate("homeKta/${it.uid}")
                                                        } else {
                                                            navController.navigate("uploadKtp")
                                                        }
                                                    }
                                                }
                                                3 -> navController.navigate("donations")
                                            }
                                        }) {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .padding(2.dp)
                                            .clip(shape = RoundedCornerShape(12.dp))
                                            .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),

                                        contentAlignment = Alignment.Center // Memastikan ikon di tengah
                                    ) {
                                        Image(painter = painterResource(icon),
                                            contentDescription = label,
                                            modifier = Modifier.size(60.dp),
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
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("aboutGrib")
                    }
                    .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = R.drawable.logo_grib),
                            contentDescription = "Logo",
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                fontSize = 11.sp,
                                lineHeight = 13.sp,
                                text = "GRIB atau Gerakan Rakyat Indonesia Bersatu didirikan oleh Rosario de Marshal, atau populer sebagai Hercules. Hercules adalah mantan gangster dan broker politik asal Timor Timur"
                            )
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
                SlideComponentBanner(items = ads.value,
                    isLoading = isLoading.value,
                    onItemClick = { actionValue ->
                        Log.d("Banner Clicked", "Aksi: $actionValue")
                    })

                PublicComplaints()
                SlideComponentNews(
                    items = NewsData.newsList, onItemClick = { menu ->
                        navController.navigate("news_detail/$menu")
                    }, navController = navController
                )
//                SlideComponentCharity(
//                    items = charitys,
//                    onItemClick = { menu ->
//                        println("Menu yang diklik: $menu")
//                    }
//                )
                SlideComponentProduct(items = products, onItemClick = { menu ->
                    println("Menu yang diklik: $menu")
                })

                if (showBottomSheet) {
                    ShareBottomSheet(
                        onDismiss = {
                            showBottomSheet = false
                        }, link = "", title = "Hello GRIB", content = listOf(
                            Triple(
                                "Image",
                                "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEhuN3iKdunuG0uNVvXu9AuT0s8HfK-O7zDytNKuKvMmv2H_pUpPXW7KKG7FIWA9TcqlH6QjNOaCtm-L3sr4FQu92A6VPFLtELXjPN3x1IjRixtDmLvZ8ofeNXeGr9hzrQjYyTm5HyrNDFU/s1600/LOGO+GRIB.jpg",
                                ""
                            ), Triple(
                                "Text",
                                "Bergabunglah bersama Hello GRIB, mulailah terhubung dengan Organisasii komunitas terbesar di Indonesia. Temukan banyak teman, sahabat, hobby, minat, bangkit dan maju bersama untuk Indonesia Jaya ",
                                ""
                            )
                        )
                    )
                }
            }

        }

    }
}
