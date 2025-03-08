package com.example.test.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.test.R
import com.example.test.ui.dataType.News
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior

@OptIn(ExperimentalSnapperApi::class)
@Composable
fun ListComponentNews(
    items: List<News>, // List berisi pasangan ikon & teks
    onItemClick: (String) -> Unit,
    title: String? = null,
    moreText: String? = "Lihat Semua",
    navController: NavHostController
) {
    val lazyListState = rememberLazyListState()

    Column(modifier = Modifier.padding(vertical = 8.dp).padding(bottom = 16.dp).fillMaxWidth()) {
        title.let {
            if (it != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp).fillMaxWidth(), horizontalArrangement =  Arrangement.SpaceBetween) {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if(moreText != "" && moreText != null) {
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
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            items.forEach() { item ->
                Row(
                    modifier = Modifier
                        .clickable { onItemClick(item.id) }
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically // Pastikan sejajar
                ) {
                    Column(
                        modifier = Modifier.weight(1f) // Memberi ruang fleksibel untuk teks
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                item.author.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                formatTimeAgo(item.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    AsyncImage(
                        model = item.thumbnailUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 120.dp, height = 80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }

}