package com.example.test.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.test.R
import com.example.test.ui.components.SlideComponentBanner
import com.example.test.ui.components.SlideComponentNews
import com.example.test.ui.dataTest.banners
import com.example.test.ui.dataType.Address
import com.example.test.ui.dataType.Branch
import com.example.test.ui.dataType.Member
import com.example.test.ui.viewModels.MemberViewModel
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeKtaScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    userId: String,
    memberViewModel: MemberViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(true) }
    var member by remember { mutableStateOf<Member?>(null) }
    var branch by remember { mutableStateOf<Branch?>(null) }

    val items = listOf(
        "KTA Digital" to R.drawable.baseline_card_membership_24,
        "Biodata" to R.drawable.baseline_person_24,
        "Dokumen" to R.drawable.baseline_description_24,
    )

    val columns = 3
    val gridState = rememberLazyGridState()

    LaunchedEffect(userId) {
        memberViewModel.fetchMember(userId) { fetchedMember, fetchedBranch ->
            member = fetchedMember
            branch = fetchedBranch
            isLoading = false
        }
    }

    Log.d("HomeKtaScreen", "Member: ${member?.fotoUrl}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KTA") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                .verticalScroll(rememberScrollState()) // 🔥 Pastikan bisa discroll
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {


                // 🔹 Profil Member
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = R.drawable.logo_grib),
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Identitas Keanggotaan", fontWeight = FontWeight.Bold)
                        Text(text = "Digital", fontSize = 14.sp,letterSpacing = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    // ✅ Bungkus dalam Box agar ukuran tetap 3x4 (90x120dp)
                    Box(
                        modifier = Modifier
                            .size(width = 90.dp, height = 120.dp)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = member?.fotoUrl,
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop,

                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(text = member?.fullName ?: "Nama Anggota", fontWeight = FontWeight.Bold)
                    Text(text = "ID : ${member?.memberId ?: "GRIB001"}", fontWeight = FontWeight.Bold)
                }

                Divider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // 🔹 Grid Menu
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    state = gridState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp) // 🔥 Solusi: Tetapkan tinggi agar tidak infinite
                        .padding(horizontal = 16.dp)
                ) {
                    items(items) { (label, icon) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    when (label) {
                                        "KTA Digital" -> {
                                            navController.navigate("digitalCard/$userId")

                                        }

                                        "Biodata" -> {
                                            navController.navigate("biodataMember/$userId")
                                        }

                                        "Dokumen" -> navController.navigate("dokumenMember/$userId")
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = icon),
                                    contentDescription = label,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.White
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
    }
}






