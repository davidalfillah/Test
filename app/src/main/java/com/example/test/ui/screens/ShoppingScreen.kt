package com.example.test.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import com.example.test.R
import com.example.test.ui.components.SlideComponentBanner
import com.example.test.ui.components.SlideComponentNews
import com.example.test.ui.components.SlideComponentProduct
import com.example.test.ui.components.SlideShoppingCategorys
import com.example.test.ui.dataTest.CategoryButtons
import com.example.test.ui.dataTest.NewsData
import com.example.test.ui.dataTest.banners
import com.example.test.ui.dataTest.products

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(navController: NavHostController, paddingValues: PaddingValues) {
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
                    items = banners.shuffled().take(3),
                    onItemClick = { menu ->
                        println("Menu yang diklik: $menu")
                    },
                    scrollInterval = 6000L
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