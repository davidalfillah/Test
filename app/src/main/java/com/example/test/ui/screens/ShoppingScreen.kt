package com.example.test.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import com.example.test.R
import com.example.test.ui.components.SlideComponentBanner
import com.example.test.ui.components.SlideComponentProduct
import com.example.test.ui.components.SlideShoppingCategorys
import com.example.test.ui.dataTest.CategoryButtons
import com.example.test.ui.dataTest.banners
import com.example.test.ui.dataTest.products
import com.example.test.ui.viewModels.Ad
import com.example.test.ui.viewModels.AdViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(navController: NavHostController, paddingValues: PaddingValues) {
    val ads = remember { mutableStateOf(emptyList<Ad>()) }
    val isLoading = remember { mutableStateOf(true) } // Status loading
    val adViewModel = AdViewModel()

    LaunchedEffect(Unit) {
        adViewModel.getAds { fetchedAds ->
            ads.value = fetchedAds
            isLoading.value = false // Matikan loading setelah data diambil
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marketplace UMKM") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = { /* Aksi ketika tombol pencarian ditekan */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { /* Aksi ketika tombol pencarian ditekan */ }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_checklist_rtl_24),
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onBackground
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
            Column() {
                SlideComponentBanner(
                    items = ads.value,
                    isLoading = isLoading.value,
                    onItemClick = { actionValue ->
                        Log.d("Banner Clicked", "Aksi: $actionValue")
                    }
                )
                SlideShoppingCategorys(
                    items = CategoryButtons.categoryButton,
                    onItemClick = { selectedCategory ->
                        println("Kategori dipilih: $selectedCategory")
                    },
                    navController = navController
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