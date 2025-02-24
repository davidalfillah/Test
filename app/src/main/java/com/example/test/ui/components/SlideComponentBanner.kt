package com.example.test.ui.components

import android.media.Image
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalSnapperApi::class)
@Composable
fun SlideComponentBanner(
    items: List<Pair<Int, String>>,
    onItemClick: (String) -> Unit,
    scrollInterval: Long = 3000L // Auto-scroll setiap 3 detik
) {
    val lazyListState = rememberLazyListState()
    val configuration = LocalConfiguration.current

    // Ukuran dinamis berdasarkan layar
    val screenWidth = configuration.screenWidthDp.dp
    val bannerWidth = screenWidth * 0.9f // Banner 85% dari lebar layar
    val bannerHeight = bannerWidth * 0.5f // Rasio 16:9

    // Duplikasi item pertama di akhir list agar transisi lebih mulus
    val loopItems = items + items.first()

    LaunchedEffect(Unit) {
        while (isActive) { // Loop berjalan terus
            delay(scrollInterval)

            val nextIndex = lazyListState.firstVisibleItemIndex + 1

            if (nextIndex < loopItems.size) {
                lazyListState.animateScrollToItem(nextIndex)
            }

            // Jika sudah mencapai item terakhir (duplikat), langsung lompat ke awal
            if (nextIndex == loopItems.size - 1) {
                delay(scrollInterval) // Kasih delay sejenak agar tidak terlihat melompat
                lazyListState.scrollToItem(0)
            }
        }
    }

    LazyRow(
        state = lazyListState,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), // Jarak antar item
        contentPadding = PaddingValues(horizontal = 24.dp), // Padding kiri & kanan
        flingBehavior = rememberSnapperFlingBehavior(lazyListState)
    ) {
        items(loopItems) { item ->
            Image(
                painter = painterResource(id = item.first),
                contentDescription = "Banner Image",
                modifier = Modifier
                    .width(bannerWidth)  // Ukuran dinamis
                    .height(bannerHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onItemClick(item.second) },
                contentScale = ContentScale.Crop
            )
        }
    }
}



