package com.example.test.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.test.R
import com.example.test.ui.components.ShareBottomSheet
import com.example.test.ui.components.UserProfileImage
import com.example.test.ui.components.formatNewsContent
import com.example.test.ui.components.formatTimeAgo
import com.example.test.ui.dataType.News
import com.example.test.ui.dataType.NewsContent
import com.example.test.ui.viewModels.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    newsId: String,
    navController: NavHostController,
    newsViewModel: NewsViewModel = viewModel()
) {
    var news by remember { mutableStateOf<News?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(newsId) {
        newsViewModel.fetchNewsById(
            newsId = newsId,
            onLoading = { isLoading = true },
            onSuccess = { fetchedNews ->
                news = fetchedNews
                isLoading = false
                error = null
            },
            onError = { errorMessage ->
                error = errorMessage
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_bookmark_24),
                            contentDescription = "Bookmark"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = error ?: "Unknown error occurred",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                news != null -> {
                    NewsContents(
                        news = news!!,
                        navController = navController,
                        onShareClick = { showBottomSheet = true }
                    )
                }
            }

            if (showBottomSheet) {
                ShareBottomSheet(
                    onDismiss = { showBottomSheet = false },
                    link = news?.id ?: "",
                    title = "Bagikan Berita"
                )
            }
        }
    }
}

@Composable
private fun NewsContents(
    news: News,
    navController: NavHostController,
    onShareClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            model = news.thumbnailUrl,
            contentDescription = news.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable {
                    val imageUrls = listOf(
                        mapOf("url" to news.thumbnailUrl, "title" to news.title)
                    )
                    navController.currentBackStackEntry?.savedStateHandle?.set("imageUrls", imageUrls)
                    navController.navigate("fullscreen/0")
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
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserProfileImage(news.author.profilePicUrl, 48)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "By ${news.author.name}",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    formatTimeAgo(news.createdAt),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionItem(
                    iconRes = R.drawable.outline_comment_24,
                    text = "${news.comments.size} comments"
                )
                ActionItem(
                    iconRes = R.drawable.baseline_favorite_border_24,
                    text = "${news.likes.size} likes"
                )
                ActionItem(
                    iconRes = R.drawable.outline_share_24,
                    text = "Share",
                    onClick = onShareClick
                )
            }

            news.content.forEach { content ->
                NewsContentItem(
                    content = content,
                    onImageClick = { imageUrl ->
                        val imageUrls = listOf(mapOf("url" to imageUrl, "title" to news.title))
                        navController.currentBackStackEntry?.savedStateHandle?.set("imageUrls", imageUrls)
                        navController.navigate("fullscreen/0")
                    },
                    onVideoClick = { videoUrl ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                        LocalContext.current.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp)) // Jarak antar konten
            }

            if (news.content.isEmpty()) {
                Text(
                    text = "Tidak ada konten tersedia",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}


@Composable
fun NewsContentItem(
    content: NewsContent,
    onImageClick: (String) -> Unit,
    onVideoClick: @Composable (String) -> Unit
) {
    when {
        content.text?.isNotBlank() == true -> {
            // Menampilkan konten teks
            Text(
                text = content.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
        content.imageUrl?.isNotBlank() == true -> {
            // Menampilkan konten gambar
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(content.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = content.caption ?: "Gambar berita",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onImageClick(content.imageUrl!!) }
            )
            content.caption?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 8.dp)
                )
            }
        }
        content.videoUrl?.isNotBlank() == true -> {
            // Menampilkan konten video
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
//                    .clickable { onVideoClick(content.videoUrl!!) }
            ) {
                content.videoThumbnailUrl?.let { thumbnail ->
                    AsyncImage(
                        model = thumbnail,
                        contentDescription = "Thumbnail video",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Putar video",
                    tint = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
            content.caption?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 8.dp)
                )
            }
        }
        content.articleUrl?.isNotBlank() == true -> {
            // Menampilkan tautan artikel
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
//                    .clickable {
//                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(content.articleUrl))
//                        LocalContext.current.startActivity(intent)
//                    }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Baca Juga:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = content.articleTitle ?: "Tanpa Judul",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        else -> {
            // Tampilan default jika tidak ada konten yang valid
            Text(
                text = "Konten tidak dikenali",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

/** Komponen untuk aksi seperti komentar, suka, bagikan */
@Composable
fun ActionItem(
    @DrawableRes iconRes: Int,
    text: String,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall
        )
    }
}