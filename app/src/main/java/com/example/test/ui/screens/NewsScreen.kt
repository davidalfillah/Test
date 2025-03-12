package com.example.test.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.test.ui.components.ListComponentNews
import com.example.test.ui.components.SlideComponentBanner
import com.example.test.ui.components.SlideComponentNews
import com.example.test.ui.components.formatTimeAgo
import com.example.test.ui.dataType.News
import com.example.test.ui.viewModels.Ad
import com.example.test.ui.viewModels.AdViewModel
import com.example.test.ui.viewModels.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(navController: NavHostController, paddingValues: PaddingValues, ) {
    val ads = remember { mutableStateOf(emptyList<Ad>()) }
    var newsList by remember { mutableStateOf<List<News>>(emptyList()) }
    var latestNewsList by remember { mutableStateOf<List<News>>(emptyList()) }
    var trendingNewsList by remember { mutableStateOf<List<News>>(emptyList()) }
    var bookmarkedNewsList by remember { mutableStateOf<List<News>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    var isLoadingMore by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val adViewModel = AdViewModel()
    val newsViewModel: NewsViewModel = viewModel()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && !isLoadingMore && !isLoading) {
                    val totalItems = newsList.size
                    if (lastIndex >= totalItems - 2) {
                        isLoadingMore = true
                        newsViewModel.fetchNews(
                            isLoadMore = true,
                            onSuccess = { moreNews ->
                                if (moreNews.isEmpty()) {
                                    // Tidak ada data baru, set isLoadingMore false
                                    isLoadingMore = false
                                } else {
                                    newsList = newsList + moreNews
                                    isLoadingMore = false
                                }
                            },
                            onError = { errorMessage ->
                                error = errorMessage
                                isLoadingMore = false
                            }
                        )
                    }
                }
            }
    }


    LaunchedEffect(Unit) {
        newsViewModel.fetchNews(
            isLoadMore = false,
            onLoading = { isLoading = true },
            onSuccess = { fetchedNews ->
                newsList = fetchedNews
                isLoading = false
                error = null
            },
            onError = { errorMessage ->
                error = errorMessage
                isLoading = false
            }
        )
        newsViewModel.fetchTrendingNews(
            onLoading = { isLoading = true },
            onSuccess = { fetchedTrendingNews ->
                // Filter trendingNews agar tidak ada yang sudah di latestNewsList
                val latestIds = latestNewsList.map { it.id }.toSet()
                trendingNewsList = fetchedTrendingNews.filter { it.id !in latestIds }
                isLoading = false
                error = null
            },
            onError = { errorMessage ->
                error = errorMessage
                isLoading = false
            }
        )
        newsViewModel.fetchLatestNews(
            onLoading = { isLoading = true },
            onSuccess = { fetchedLatestNews ->
                // Filter latestNews agar tidak ada yang sudah di trendingNewsList
                val trendingIds = trendingNewsList.map { it.id }.toSet()
                latestNewsList = fetchedLatestNews.filter { it.id !in trendingIds }
                isLoading = false
                error = null
            },
            onError = { errorMessage ->
                error = errorMessage
                isLoading = false
            }
        )
        newsViewModel.fetchBookmarkedNews(
            onLoading = { isLoading = true },
            onSuccess = { bookmarkNews ->
                bookmarkedNewsList = bookmarkNews
                isLoading = false
                error = null
            },
            onError = { errorMessage ->
                error = errorMessage
                isLoading = false
            }
        )
    }

    LaunchedEffect(Unit) {
        adViewModel.getAds { fetchedAds ->
            ads.value = fetchedAds
            isLoading = false // Matikan loading setelah data diambil
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("News") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("searchNews") }) { // Navigasi ke SearchScreen
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                )

                // Tab Bar
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Semua") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Bookmark") }
                    )
                }
            }

        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(
                    text = error ?: "Terjadi kesalahan",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    when (selectedTabIndex) {
                        0 -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    SlideComponentBanner(
                                        items = ads.value,
                                        isLoading = isLoading,
                                        onItemClick = { actionValue ->
                                            Log.d("Banner Clicked", "Aksi: $actionValue")
                                        },
                                    )
                                }

                                // Latest news section
                                if (latestNewsList.isNotEmpty()) {
                                    item {
                                        SlideComponentNews(
                                            items = latestNewsList,
                                            onItemClick = { id -> navController.navigate("news_detail/${id}") },
                                            navController = navController,
                                            title = "Berita Terbaru",
                                        )
                                    }
                                }

                                // Trending news section
                                if (trendingNewsList.isNotEmpty()) {
                                    item {
                                        SlideComponentNews(
                                            items = trendingNewsList,
                                            onItemClick = { id -> navController.navigate("news_detail/${id}") },
                                            navController = navController,
                                            title = "Berita Populer",
                                        )
                                    }
                                }

                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Semua Berita",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleLarge,
                                        )
                                    }
                                }

                                // All news items
                                items(newsList) { news ->
                                    Row(
                                        modifier = Modifier
                                            .clickable {
                                                navController.navigate("news_detail/${news.id}")
                                            }
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically // Pastikan sejajar
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f) // Memberi ruang fleksibel untuk teks
                                        ) {
                                            Text(
                                                text = news.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                modifier = Modifier.padding(top = 8.dp)
                                            ) {
                                                Text(
                                                    news.author.name,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    formatTimeAgo(news.createdAt),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        AsyncImage(
                                            model = news.thumbnailUrl,
                                            contentDescription = news.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(width = 120.dp, height = 80.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                }

                                // Loading indicator at bottom
                                item {
                                    if (isLoadingMore) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    } else if (newsViewModel.isLastPage) { // Tambahkan properti isLastPage sebagai public di ViewModel
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Tidak ada berita lagi",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                        }
                        1 -> {
                            // Tab "Bookmark" - Konten berita yang di-bookmark
                            if (bookmarkedNewsList.isEmpty()) {
                                Text(
                                    text = "Belum ada berita yang di-bookmark.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                ListComponentNews(
                                    items = bookmarkedNewsList,
                                    onItemClick = { id ->
                                        navController.navigate("news_detail/${id}")
                                    },
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}