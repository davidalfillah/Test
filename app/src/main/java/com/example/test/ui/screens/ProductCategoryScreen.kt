package com.example.test.ui.screens

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.test.R
import com.example.test.ui.components.ListComponentNews
import com.example.test.ui.components.SlideComponentBanner
import com.example.test.ui.components.SlideComponentNews
import com.example.test.ui.dataType.ProductCategory
import com.example.test.ui.dataType.Subcategory
import com.example.test.ui.viewModels.ProductViewModel

@Composable
fun CollapsibleItem(
    title: String,
    buttons: List<Subcategory>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("CollapsibleItem", "Title: $title, Buttons: $buttons, IsExpanded: $isExpanded")

    Column(
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Row(
            modifier = Modifier
                .clickable { onExpandChange(!isExpanded) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Expand/Collapse",
                modifier = Modifier.graphicsLayer {
                    rotationZ = if (isExpanded) 180f else 0f
                }
            )
        }

        if (isExpanded) {
            if (buttons.isEmpty()) {
                Text(
                    text = "Tidak ada subkategori",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                // Batasi tinggi maksimum LazyVerticalGrid
                Box(
                    modifier = Modifier
                        .heightIn(max = 300.dp) // Batas tinggi maksimum
                        .fillMaxWidth()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    ) {
                        items(buttons) { button ->
                            val iconRes = when (button.id.lowercase()) {
                                "automotive" -> R.drawable.baseline_directions_car_24
                                "baby_kids" -> R.drawable.baseline_baby_changing_station_24
                                "books" -> R.drawable.baseline_menu_book_24
                                "computers" -> R.drawable.baseline_computer_24
                                "digital_products" -> R.drawable.baseline_screen_search_desktop_24
                                "electronics" -> R.drawable.baseline_cable_24
                                "fashion" -> R.drawable.baseline_checkroom_24
                                "food_beverages" -> R.drawable.baseline_fastfood_24
                                "gaming" -> R.drawable.baseline_videogame_asset_24
                                "health_beauty" -> R.drawable.baseline_face_retouching_natural_24
                                "hobbies_sports" -> R.drawable.baseline_sports_kabaddi_24
                                "home_appliances" -> R.drawable.baseline_house_siding_24
                                "home_living" -> R.drawable.baseline_all_inbox_24
                                "industrial" -> R.drawable.baseline_engineering_24
                                "jewelry_watches" -> R.drawable.baseline_watch_24
                                "mobile_accessories" -> R.drawable.baseline_smartphone_24
                                "pets" -> R.drawable.baseline_pets_24
                                "tickets_vouchers" -> R.drawable.baseline_book_online_24
                                "travel" -> R.drawable.baseline_travel_explore_24
                                else -> R.drawable.baseline_more_vert_24
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {  }
                                    .padding(8.dp)
                                    .width(56.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = button.name,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = button.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2, // Membatasi maksimal 2 baris
                                    overflow = TextOverflow.Ellipsis, // Jika lebih dari 2 baris, akan muncul titik "..."
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollapsibleListScreen(categories: List<ProductCategory>) {
    var expandedId by remember { mutableStateOf<String?>(null) }

    if (categories.isEmpty()) {
        Text(
            text = "Tidak ada kategori tersedia",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    } else {
        LazyColumn {
            items(categories) { category ->
                CollapsibleItem(
                    title = category.name,
                    buttons = category.subcategories,
                    isExpanded = expandedId == category.id,
                    onExpandChange = { shouldExpand ->
                        expandedId = if (shouldExpand) category.id else null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCategoryScreen(navController: NavHostController, paddingValues: PaddingValues) {

    val productViewModel = ProductViewModel()

    var categories by remember { mutableStateOf<List<ProductCategory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        productViewModel.fetchCategories { result ->
            categories = result
            isLoading = false
            error = null
        }
    }

    Scaffold(
        topBar =
        {
            Column {
                TopAppBar(
                    title = { Text("Kategori Produk") },
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
                )


            }

        }
    )
    { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }else{
                CollapsibleListScreen(categories)
            }
        }
    }
}