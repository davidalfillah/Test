package com.example.test.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.test.ui.components.formatNewsContent
import com.example.test.ui.dataTest.NewsArticle
import com.example.test.ui.dataTest.NewsData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(newsId: String, navController:NavHostController) {
    // Simulasi data berdasarkan ID (Bisa diganti dengan data dari API atau Database)
    val news = remember(newsId) { getNewsById(newsId) }

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
                            imageVector = Icons.Default.ArrowBack, // Ikon panah kembali
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Share, // Ikon pencarian
                            contentDescription = "hare",
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = news.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AsyncImage(
                model = news.imageUrl,
                contentDescription = news.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 16.dp)
            )

            // Isi Berita
            Text(
                text = formatNewsContent(news.content),
                fontSize = 16.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

// Fungsi untuk mendapatkan berita berdasarkan ID (Simulasi)
fun getNewsById(articleId: String): NewsArticle {
    val dummyNews = NewsData.newsList
    return dummyNews.find { it.articleId == articleId } ?: dummyNews.first()
}