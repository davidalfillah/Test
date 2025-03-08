package com.example.test.ui.components

import android.media.Image
import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.test.R
import com.example.test.ui.dataType.News
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalSnapperApi::class)
@Composable
fun SlideComponentNews(
    items: List<News>, // List berisi pasangan ikon & teks
    onItemClick: (String) -> Unit,
    title: String? = null,
    moreText: String? = "Lihat Semua",
    navController: NavHostController
) {
    val lazyListState = rememberLazyListState()

    Column(modifier = Modifier
        .padding(vertical = 8.dp)
        .padding(bottom = 16.dp)
        .fillMaxWidth()) {

        title.let {
            if (it != null) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (moreText != "" && moreText != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp), // Pastikan item berada di tengah layar
            flingBehavior = rememberSnapperFlingBehavior(lazyListState)
        ) {
            items(items) { item ->
                OutlinedCard(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .width(300.dp)
                        .clickable { onItemClick(item.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column {
                        AsyncImage(
                            model = item.thumbnailUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop, // Gambar diisi penuh & dipotong jika perlu
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp) // Bagian gambar lebih dominan

                        )

                        // Konten Berita
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    item.author.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    formatTimeAgo(item.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                            }
                        }
                    }
                }
            }
        }
    }

}
