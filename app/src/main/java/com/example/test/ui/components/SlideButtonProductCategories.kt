package com.example.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.test.R
import com.example.test.ui.dataType.ProductCategory
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior

@OptIn(ExperimentalSnapperApi::class)
@Composable
fun SlideButtonProductCategories(
    items: List<ProductCategory>, // List berisi pasangan ikon & teks
    onItemClick: (String) -> Unit,
    title: String? = null,
    moreText: String? = null,
    moreTextClick: ((String) -> Unit)? = null,
    navController: NavHostController
) {
    val lazyListState = rememberLazyListState()

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        title.let {
            if (it != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp).fillMaxWidth(), horizontalArrangement =  Arrangement.SpaceBetween) {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (moreText != "" && moreText != null && moreTextClick != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(
                            onClick = {
                                moreTextClick(moreText)
                            }
                        )) {
                            Text(
                                text = moreText,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_forward_ios_24),
                                contentDescription = "Donasi",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary // Warna ikon agar kontras
                            )
                        }
                    }
                }
            }
        }
        LazyRow(
            state = lazyListState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            flingBehavior = rememberSnapperFlingBehavior(lazyListState)
        ) {
            items(items) { item ->
                CategoryItem(category = item, onClick = onItemClick)
            }
        }
    }
}



// Item kategori yang bisa diklik
@Composable
fun CategoryItem(category: ProductCategory, onClick: (String) -> Unit) {
    val iconRes = when (category.id) {
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
            .clickable { onClick(category.id) }
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
                contentDescription = category.name,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2, // Membatasi maksimal 2 baris
            overflow = TextOverflow.Ellipsis, // Jika lebih dari 2 baris, akan muncul titik "..."
            modifier = Modifier.fillMaxWidth()
        )
    }
}

