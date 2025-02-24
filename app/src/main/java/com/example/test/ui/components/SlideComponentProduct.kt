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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.size.Size
import com.example.test.R
import com.example.test.ui.screens.Product
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import kotlin.math.max

@OptIn(ExperimentalSnapperApi::class)
@Composable
fun SlideComponentProduct(
    items: List<Product>,
    onItemClick: (String) -> Unit,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardSize = 160.dp // Ukuran default per card
    val columns = max(1, (screenWidth / cardSize).toInt()) // Hitung jumlah kolom yang muat





    Column(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Produk & Jasa UMKM", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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

        // Membagi item menjadi kelompok berisi 2 item per baris
        val chunkedItems = items.chunked(columns)

        Column(modifier = Modifier.fillMaxWidth()) {
            chunkedItems.forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp)) // Bisa dihapus, karena Card sudah bisa berbentuk rounded
                                .clickable { /* Aksi ketika item diklik */ }
                                .background(color = MaterialTheme.colorScheme.surfaceContainerLowest)
                        ) {
                            Column {
                                AsyncImage(
                                    model = item.image,
                                    contentDescription = "Gambar dengan Placeholder",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f) // Gambar 1:1
                                )
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 12.sp,
                                        color = Color.Black,
                                        lineHeight = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${formatCurrency2(item.price.toLong())}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Jika jumlah item ganjil, tambahkan Spacer agar grid tetap rapi
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
