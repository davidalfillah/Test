package com.example.test.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.test.R
import com.example.test.ui.components.ShareBottomSheet
import com.example.test.ui.components.UserProfileImage
import com.example.test.ui.components.formatNewsContent
import com.example.test.ui.dataTest.NewsArticle
import com.example.test.ui.dataTest.NewsData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(newsId: String, navController:NavHostController) {
    // Simulasi data berdasarkan ID (Bisa diganti dengan data dari API atau Database)
    val news = remember(newsId) { getNewsById(newsId) }
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )  },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showBottomSheet = true
                    },) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_bookmark_24),
                            contentDescription = "boorkmark",
                        )
                    }
                }
            )

        }
    ){ paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = news.imageUrl,
                contentDescription = news.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable {
                        val imageUrls = listOf(
                            mapOf("url" to news.imageUrl, "title" to news.title)
                        )

                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("imageUrls", imageUrls)

                        val startIndex = imageUrls.indexOfFirst { it["url"] == news.imageUrl }

                        navController.navigate("fullscreen/$startIndex")
                    }
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = news.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
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

                        UserProfileImage("", 48)

                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "By ${news.sourceName}",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.wrapContentWidth()
                        )
                    }
                    Text(
                        text = news.pubDate,
                        style = MaterialTheme.typography.titleSmall,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                ){
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_comment_24),
                            contentDescription = "hare",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "8 comments",
                            style = MaterialTheme.typography.titleSmall,
                            lineHeight = 14.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_favorite_border_24),
                            contentDescription = "hare",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "36 likes",
                            style = MaterialTheme.typography.titleSmall,
                            lineHeight = 14.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_share_24),
                            contentDescription = "hare",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share",
                            style = MaterialTheme.typography.titleSmall,
                            lineHeight = 14.sp,
                        )
                    }
                }
                Text(
                    text = formatNewsContent(news.content),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        if (showBottomSheet) {
            ShareBottomSheet(
                onDismiss = {
                    showBottomSheet = false
                }, link = "", title = "Bagikan Berita",
            )
        }
    }
}

// Fungsi untuk mendapatkan berita berdasarkan ID (Simulasi)
fun getNewsById(articleId: String): NewsArticle {
    val dummyNews = NewsData.newsList
    return dummyNews.find { it.articleId == articleId } ?: dummyNews.first()
}