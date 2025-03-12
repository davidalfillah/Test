package com.example.test.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.test.ui.dataType.Branch
import com.example.test.ui.dataType.Member
import com.example.test.ui.viewModels.MemberViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DokumenMemberScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    memberId: String,
    memberViewModel: MemberViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(true) }
    var files by remember { mutableStateOf<List<Map<String, Any>>?>(null) }

    LaunchedEffect(memberId) {
        memberViewModel.getFilesByMemberId(
            memberId,
            onSuccess = {
                files = it
                isLoading = false
            },
            onFailure = {
                Log.e("DokumenMemberScreen", "Gagal mengambil data file: $it")
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dokumen Member") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                if (files.isNullOrEmpty()) {
                    Text(
                        text = "Tidak ada dokumen",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        // Hilangkan contentPadding agar tidak ada padding tambahan
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(files!!) { file ->
                            FileItem(
                                fileName = file["fileName"] as? String ?: "Unknown",
                                fileType = file["fileType"] as? String ?: "Unknown",
                                fileUrl = file["fileUrl"] as? String ?: "",
                                onClick = {
                                    // Buat daftar file dalam format yang sama
                                    val imageData = files!!.map {
                                        mapOf(
                                            "url" to (it["fileUrl"] as? String ?: ""),
                                            "title" to (it["fileName"] as? String ?: "No Title")
                                        )
                                    }

                                    // Simpan daftar file ke savedStateHandle sebelum navigasi
                                    navController.currentBackStackEntry?.savedStateHandle?.set("imageUrls", imageData)

                                    // Cari index gambar yang diklik
                                    val startIndex = files!!.indexOf(file)

                                    // Panggil layar fullscreen dengan startIndex
                                    navController.navigate("fullscreen/$startIndex")
                                }
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun FileItem(
    fileName: String,
    fileType: String,
    fileUrl: String,
    onClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick(fileUrl) },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                fileType.contains("image", true) || fileType in listOf("KTP", "KTA") -> {
                    AsyncImage(
                        model = fileUrl,
                        contentDescription = fileName,
                        modifier = Modifier
                            .fillMaxWidth() // Lebar penuh
                            .aspectRatio(1f) // Agar berbentuk kotak
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                }
                fileType.contains("pdf", true) -> {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = fileName,
                        tint = Color.Red,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(16.dp)
                    )
                }
                fileType.contains("doc", true) || fileType.contains("docx", true) -> {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = fileName,
                        tint = Color.Blue,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(16.dp)
                    )
                }
                fileType.contains("xls", true) || fileType.contains("xlsx", true) -> {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = fileName,
                        tint = Color.Green,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(16.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = fileName,
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(16.dp)
                    )
                }
            }
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}
