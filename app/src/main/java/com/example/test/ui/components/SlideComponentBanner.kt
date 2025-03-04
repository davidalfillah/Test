package com.example.test.ui.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil3.compose.rememberAsyncImagePainter
import com.example.test.ui.viewModels.Ad
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SlideComponentBanner(
    items: List<Ad>,
    isLoading: Boolean,
    onItemClick: (String) -> Unit,
    scrollInterval: Long = 6000L
) {
    val pagerState = rememberPagerState { items.size }
    val configuration = LocalConfiguration.current
    val isAutoScrolling = remember { mutableStateOf(true) } // Kontrol auto-scroll
    val isUserInteracting = remember { mutableStateOf(false) } // Deteksi interaksi pengguna
    val coroutineScope = rememberCoroutineScope()

    val screenWidth = configuration.screenWidthDp.dp
    val bannerWidth = screenWidth * 0.88f
    val bannerHeight = bannerWidth * 0.5f

    // Auto-scroll effect
    LaunchedEffect(items, isAutoScrolling.value) {
        if (items.isNotEmpty() && !isLoading && isAutoScrolling.value) {
            while (isActive) {
                delay(scrollInterval)
                val nextPage = (pagerState.currentPage + 1) % items.size
                coroutineScope.launch {
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        animationSpec = tween(
                            durationMillis = 800,
                            easing = LinearEasing
                        )
                    )
                }
            }
        }
    }

    // Deteksi interaksi pengguna
    LaunchedEffect(Unit) {
        snapshotFlow { isUserInteracting.value }
            .collect { interacting ->
                if (interacting) {
                    isAutoScrolling.value = false // Jeda saat pengguna menyentuh
                } else {
                    delay(5000L) // Lanjutkan setelah 5 detik tanpa interaksi
                    isAutoScrolling.value = true
                }
            }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Blue
                )
            }
            items.isEmpty() -> {
                Text(
                    text = "Tidak ada iklan tersedia",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
            else -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { isUserInteracting.value = true },
                                onDragEnd = { isUserInteracting.value = false },
                                onDragCancel = { isUserInteracting.value = false },
                                onDrag = { change, _ ->
                                    // Tandai bahwa pengguna sedang berinteraksi selama drag
                                    isUserInteracting.value = true
                                    // Konsumsi event drag agar tidak memengaruhi komponen lain
                                    change.consume()
                                }
                            )
                        },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) { page ->
                    val ad = items[page]
                    Image(
                        painter = rememberAsyncImagePainter(ad.imageUrl),
                        contentDescription = "Banner Image",
                        modifier = Modifier
                            .width(bannerWidth)
                            .height(bannerHeight)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onItemClick(ad.actionValue) }
                            .graphicsLayer {
                                val pageOffset = (
                                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                        ).absoluteValue

                                // Animasi alpha antara 50% dan 100%
                                alpha = lerp(
                                    start = 0.5f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 2f)
                                )
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}






