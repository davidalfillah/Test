package com.example.test.ui.screens

import android.net.Uri
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import com.example.test.R
import com.example.test.ui.components.SlideButtonProductCategories
import com.example.test.ui.components.SlideComponentBanner
import com.example.test.ui.components.SlideComponentProduct
import com.example.test.ui.dataTest.CategoryButtons
import com.example.test.ui.dataTest.banners
import com.example.test.ui.dataType.Product
import com.example.test.ui.dataType.ProductCategory
import com.example.test.ui.viewModels.Ad
import com.example.test.ui.viewModels.AdViewModel
import com.example.test.ui.viewModels.ProductViewModel
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(navController: NavHostController, paddingValues: PaddingValues,
                   userLat: Double,
                   userLong: Double) {
    val ads = remember { mutableStateOf(emptyList<Ad>()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val lastProduct by remember { mutableStateOf<Product?>(null) }
    val adViewModel = AdViewModel()
    val productViewModel = ProductViewModel()

    var categories by remember { mutableStateOf<List<ProductCategory>>(emptyList()) }

    LaunchedEffect(Unit) {
        productViewModel.fetchCategories { result ->
            categories = result
        }
        productViewModel.fetchProductsByLocationPaginated(
            userLat = userLat,
            userLong = userLong,
            maxDistance = 10.0, // 10km radius
            lastProduct = lastProduct,
            onSuccess = { newProducts ->
                products = if (lastProduct == null) {
                    newProducts
                } else {
                    products + newProducts
                }
                isLoading = false
            },
            onError = { message ->
                error = message
                isLoading = false
            }
        )
    }

    LaunchedEffect(Unit) {
        adViewModel.getAds { fetchedAds ->
            ads.value = fetchedAds
            isLoading = false
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
                    IconButton(onClick = {
                        navController.navigate("myProducts")
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_house_siding_24),
                            contentDescription = "MyProduct",
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
                    isLoading = isLoading,
                    onItemClick = { actionValue ->
                        Log.d("Banner Clicked", "Aksi: $actionValue")
                    }
                )
                SlideButtonProductCategories(
                    items = categories,
                    onItemClick = { selectedCategory ->
                        println("Kategori dipilih: $selectedCategory")
                    },
                    navController = navController,
                    title = "Kategori Produk",
                    moreText = "Lihat Semua",
                    moreTextClick = { selectedCategory ->
                        navController.navigate("productCategories")
                    }
                )
                SlideComponentProduct(
                    items = products,
                    onItemClick = { menu ->
                        val productJson = Uri.encode(Gson().toJson(menu))
                        navController.navigate("product_detail/$productJson")
                    }
                )

            }

        }
    }
}