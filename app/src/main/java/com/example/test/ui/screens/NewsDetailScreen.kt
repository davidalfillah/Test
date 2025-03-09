package com.example.test.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.test.R
import com.example.test.ui.components.ListComponentNews
import com.example.test.ui.components.ShareBottomSheet
import com.example.test.ui.components.UserProfileImage
import com.example.test.ui.components.formatTimeAgo
import com.example.test.ui.dataType.Comment
import com.example.test.ui.dataType.News
import com.example.test.ui.dataType.NewsContent
import com.example.test.ui.viewModels.NewsViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    newsId: String,
    navController: NavHostController,
    newsViewModel: NewsViewModel = viewModel()
) {
    var news by remember { mutableStateOf<News?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var relatedNews by remember { mutableStateOf<List<News>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showShareBottomSheet by remember { mutableStateOf(false) }
    val commentsSheetState = rememberModalBottomSheetState() // State untuk ModalBottomSheet
    var showCommentsBottomSheet by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }
    var hasUpdatedViewCount by remember { mutableStateOf(false) }


    val scrollState = rememberLazyListState()
    val isAtTop by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset == 0
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(newsId) {
        newsViewModel.fetchNewsById(
            newsId = newsId,
            onLoading = { isLoading = true },
            onSuccess = { fetchedNews ->
                news = fetchedNews
                isLoading = false
                error = null
                if (!hasUpdatedViewCount) {
                    newsViewModel.updateViewCount(newsId, fetchedNews.viewCount)
                    news = fetchedNews.copy(viewCount = fetchedNews.viewCount + 1L) // Update lokal
                    hasUpdatedViewCount = true // Tandai sudah diperbarui
                }
                newsViewModel.fetchRelatedNews(
                    currentNewsId = newsId,
                    category = fetchedNews.category,
                    onSuccess = { related -> relatedNews = related },
                    onError = { errorMsg -> error = errorMsg }
                )
            },
            onError = { errorMessage ->
                error = errorMessage
                isLoading = false
            }
        )
        newsViewModel.fetchComments(
            newsId = newsId,
            onSuccess = { fetchedComments -> comments = fetchedComments },
            onError = { errorMsg -> error = errorMsg }
        )
        newsViewModel.checkBookmarkStatus(
            newsId = newsId,
            onSuccess = { bookmarked -> isBookmarked = bookmarked },
            onError = { errorMsg -> Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!isAtTop) {
                        Text(
                            text = news?.title ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
                    IconButton(onClick = {
                        newsViewModel.toggleBookmark(
                            newsId = newsId,
                            onSuccess = { newStatus ->
                                isBookmarked = newStatus
                                Toast.makeText(
                                    context,
                                    if (isBookmarked) "Ditambahkan ke bookmark" else "Dihapus dari bookmark",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (isBookmarked) R.drawable.baseline_bookmark_24
                                else R.drawable.baseline_bookmark_border_24
                            ),
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Tombol mengambang lebih kecil untuk "Back to Top"
            if (!isAtTop) {
                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollToItem(0) // Menggulir ke atas
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .alpha(0.7f) // Sedikit transparan (70% opacity)
                        .size(32.dp), // Ukuran lebih kecil dari SmallFloatingActionButton default
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Kembali ke atas",
                        modifier = Modifier.size(18.dp) // Menyesuaikan ukuran ikon agar proporsional
                    )
                }
            }
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
                        scrollState,
                        news = news!!,
                        navController = navController,
                        onShareClick = { showShareBottomSheet = true },
                        onCommentClick = { showCommentsBottomSheet = true },
                        relatedNews = relatedNews,
                        newsViewModel = newsViewModel
                    )

                }

            }

            if (showShareBottomSheet) {
                ShareBottomSheet(
                    onDismiss = { showShareBottomSheet = false },
                    link = news?.id ?: "",
                    title = "Bagikan Berita"
                )
            }

            if (showCommentsBottomSheet) {
                ModalBottomSheet(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    onDismissRequest = {
                        showCommentsBottomSheet = false
                        coroutineScope.launch { commentsSheetState.hide() }
                    },
                    modifier = Modifier.fillMaxHeight(0.7f),
                    sheetState = rememberModalBottomSheetState()
                ) {
                    CommentsBottomSheetContent(
                        newsId = newsId,
                        comments = comments,
                        newsViewModel = newsViewModel,
                        onDismiss = { showCommentsBottomSheet = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsContents(
    scrollState: LazyListState,
    newsViewModel: NewsViewModel,
    news: News,
    navController: NavHostController,
    onShareClick: () -> Unit,
    onCommentClick: () -> Unit,
    relatedNews: List<News>? = null
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
    var isLiked by remember { mutableStateOf(news.likes.containsKey(currentUserId)) }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
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
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "imageUrls",
                            imageUrls
                        )
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
                ) {

                    UserProfileImage(news.author.profilePicUrl, 48)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "By ${news.author.name}",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "diposting " + formatTimeAgo(news.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

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
                        text = "${news.commentCount} comments",
                        onClick = onCommentClick
                    )
                    ActionItem(
                        iconRes = if(isLiked) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24,
                        text = "${news.likes.size} likes",
                        color =  if(isLiked) MaterialTheme.colorScheme.primary else null,
                        onClick = {
                            newsViewModel.toggleLike(
                                newsId = news.id,
                                userId = currentUserId,
                                onSuccess = { newLikeStatus ->
                                    isLiked = newLikeStatus
                                },
                                onError = { errorMsg ->
                                    Log.e("NewsContent", "Gagal toggle like: $errorMsg")
                                }
                            )
                        }
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
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "imageUrls",
                                imageUrls
                            )
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
        item {
            relatedNews?.let {
                ListComponentNews(
                    items = it,
                    onItemClick = { menu ->
                        navController.navigate("news_detail/$menu")
                    },
                    navController = navController,
                    title = "Lainnya yang mungkin anda suka",
                    moreText = ""
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
            )
        }

        content.imageUrl?.isNotBlank() == true -> {
            Spacer(modifier = Modifier.height(8.dp))
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
            Spacer(modifier = Modifier.height(8.dp))
        }

        content.videoUrl?.isNotBlank() == true -> {

            Spacer(modifier = Modifier.height(8.dp))
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
            Spacer(modifier = Modifier.height(8.dp))
        }

        content.articleUrl?.isNotBlank() == true -> {
            Spacer(modifier = Modifier.height(8.dp))
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
            Spacer(modifier = Modifier.height(8.dp))
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

@Composable
fun CommentsBottomSheetContent(
    newsId: String,
    comments: List<Comment>,
    newsViewModel: NewsViewModel,
    onDismiss: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope() // Tambahkan ini untuk mendapatkan CoroutineScope

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                text = "Komentar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (comments.isEmpty()) {
            Text(
                text = "Belum ada komentar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .weight(1f) // Memungkinkan teks ini mengisi ruang jika kosong
            )
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f) // Memastikan LazyColumn mengambil ruang yang tersedia
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(comments) { comment ->
                    CommentItem(
                        comment = comment,
                        isMyComment = comment.userId == currentUserId
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input untuk komentar baru
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Tulis komentar...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(12.dp)
            )
            IconButton(
                onClick = {
                    newsViewModel.addComment(
                        newsId = newsId,
                        text = commentText,
                        onSuccess = {
                            commentText = ""
                            Toast.makeText(context, "Komentar ditambahkan", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                enabled = commentText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Kirim komentar",
                    tint = if (commentText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    isMyComment: Boolean
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)

    ) {
        HorizontalDivider()
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isMyComment) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                if(
                    isMyComment
                ) {
                    Text(
                        text = " Saya ",
                        modifier = Modifier.padding(end = 4.dp).background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp),
                        ),

                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatTimeAgo(comment.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isMyComment) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )

            }
        }
    }
}


@Composable
fun ActionItem(
    @DrawableRes iconRes: Int,
    text: String,
    color: Color? = null,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            tint = color ?: MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall
        )
    }
}