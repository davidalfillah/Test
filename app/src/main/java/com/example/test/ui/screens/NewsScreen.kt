package com.example.test.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.test.ui.components.ListComponentNews
import com.example.test.ui.components.SlideComponentBanner
import com.example.test.ui.components.SlideComponentNews
import com.example.test.ui.dataTest.banners
import com.example.test.ui.dataType.News
import com.example.test.ui.viewModels.Ad
import com.example.test.ui.viewModels.AdViewModel
import com.example.test.ui.viewModels.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(navController: NavHostController, paddingValues: PaddingValues, ) {
    val ads = remember { mutableStateOf(emptyList<Ad>()) }
    var newsList by remember { mutableStateOf<List<News>>(emptyList()) }
    var bookmarkedNewsList by remember { mutableStateOf<List<News>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val adViewModel = AdViewModel()
    val newsViewModel: NewsViewModel = viewModel()


    LaunchedEffect(Unit) {
        adViewModel.getAds { fetchedAds ->
            ads.value = fetchedAds
            isLoading = false
        }
        newsViewModel.fetchNews(
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
        newsViewModel.fetchBookmarkedNews(
            onLoading = { isLoading = true },
            onSuccess = { fetchedBookmarks ->
                bookmarkedNewsList = fetchedBookmarks
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
                        IconButton(onClick = { /* Aksi ketika tombol pencarian ditekan */ }) {
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
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTabIndex) {
                        0 -> {
                            // Tab "Semua" - Konten semua berita
                            SlideComponentBanner(
                                items = ads.value,
                                isLoading = isLoading,
                                onItemClick = { actionValue ->
                                    Log.d("Banner Clicked", "Aksi: $actionValue")
                                }
                            )
                            SlideComponentNews(
                                items = newsList,
                                onItemClick = { id ->
                                    navController.navigate("news_detail/${id}")
                                },
                                navController = navController
                            )
                            ListComponentNews(
                                items = newsList,
                                onItemClick = { id ->
                                    navController.navigate("news_detail/${id}")
                                },
                                navController = navController
                            )
                            SlideComponentNews(
                                items = newsList,
                                onItemClick = { id ->
                                    navController.navigate("news_detail/${id}")
                                },
                                navController = navController
                            )
                            SlideComponentNews(
                                items = newsList,
                                onItemClick = { id ->
                                    navController.navigate("news_detail/${id}")
                                },
                                navController = navController
                            )
                            ListComponentNews(
                                items = newsList,
                                onItemClick = { id ->
                                    navController.navigate("news_detail/${id}")
                                },
                                navController = navController
                            )
                            SlideComponentBanner(
                                items = ads.value,
                                isLoading = isLoading,
                                onItemClick = { actionValue ->
                                    Log.d("Banner Clicked", "Aksi: $actionValue")
                                }
                            )
                            ListComponentNews(
                                items = newsList,
                                onItemClick = { id ->
                                    navController.navigate("news_detail/${id}")
                                },
                                navController = navController
                            )
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