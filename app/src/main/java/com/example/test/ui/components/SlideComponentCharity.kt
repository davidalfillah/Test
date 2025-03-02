package com.example.test.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.test.R
import com.example.test.ui.viewModels.Donation
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior


@OptIn(ExperimentalSnapperApi::class)
@Composable
fun SlideComponentCharity(
    items: List<Donation>, // List berisi pasangan ikon & teks
    onItemClick: (String) -> Unit,
) {
    val lazyListState = rememberLazyListState()

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp).fillMaxWidth(), horizontalArrangement =  Arrangement.SpaceBetween) {
            Text(text = "Donasi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Lihat Semua", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_forward_ios_24),
                    contentDescription = "Donasi",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary // Warna ikon agar kontras
                )
            }
        }
        LazyRow(
            state = lazyListState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp), // Pastikan item berada di tengah layar
            flingBehavior = rememberSnapperFlingBehavior(lazyListState)
        ) {
            items(items) { item ->
                Box(
                    modifier = Modifier
                        .width(147.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onItemClick(item.title) }
                        .background(color = MaterialTheme.colorScheme.surfaceContainerLowest)
                ) {
                    Column {
                        AsyncImage(
                            model = item.thumbnailUrl,
                            contentDescription = "Judul Berita",
                            contentScale = ContentScale.Crop, // Gambar diisi penuh & dipotong jika perlu
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f) // Gambar 1:1

                        )

                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = item.title,
                                fontSize = 10.sp,
                                color = Color.Black,
                                lineHeight = 11.sp,
                                maxLines = 2, // Maksimal 2 baris
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(color = Color.Red)// Warna latar belakang
                            ) {
                                LinearProgressIndicator(
                                    progress = item.totalCollected.toFloat() / item.targetAmount?.toFloat()!!,
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color.Red,
                                )
                            }

                            // Info Donasi
                            Text(
                                text = "${formatCurrency(item.totalCollected.toInt())} dari ${item.targetAmount?.toInt()
                                    ?.let { formatCurrency(it) }}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

}