package com.example.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

enum class CardOrientation {
    VERTICAL,
    HORIZONTAL
}

// Data class untuk konfigurasi card
data class CardConfig(
    val width: Dp = 200.dp,
    val height: Dp? = null, // Null berarti menyesuaikan konten
    val imageUrl: String? = null,
    val imageHeight: Dp = 120.dp,
    val title: String? = null,
    val subtitle: String? = null,
    val actionText: String? = null,
    val onActionClick: (() -> Unit)? = null,
    val backgroundColor: Color = Color.White,
    val titleStyle: TextStyle = TextStyle(fontSize = 16.sp),
    val subtitleStyle: TextStyle = TextStyle(fontSize = 12.sp, color = Color.Gray),
    val actionStyle: TextStyle = TextStyle(fontSize = 14.sp, color = Color.Blue),
    val cornerRadius: Dp = 8.dp,
    val padding: Dp = 8.dp,
    val orientation: CardOrientation = CardOrientation.VERTICAL
)

@Composable
fun DynamicCard(
    config: CardConfig,
    modifier: Modifier = Modifier
) {
    val cardModifier = modifier
        .width(config.width)
        .then((config.height?.let { Modifier.height(it) } ?: Modifier.wrapContentHeight()) as Modifier)
        .clip(RoundedCornerShape(config.cornerRadius))
        .background(config.backgroundColor)
        .padding(config.padding)

    when (config.orientation) {
        CardOrientation.VERTICAL -> {
            Column(
                modifier = cardModifier,
                horizontalAlignment = Alignment.Start
            ) {
                // Gambar (jika ada)
                config.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Card Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(config.imageHeight)
                            .clip(RoundedCornerShape(topStart = config.cornerRadius, topEnd = config.cornerRadius)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Konten teks
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(config.padding)
                ) {
                    config.title?.let {
                        Text(
                            text = it,
                            style = config.titleStyle
                        )
                    }
                    config.subtitle?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = config.subtitleStyle
                        )
                    }
                    config.actionText?.let { action ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = action,
                            style = config.actionStyle,
                            modifier = Modifier
                                .clickable { config.onActionClick?.invoke() }
                        )
                    }
                }
            }
        }

        CardOrientation.HORIZONTAL -> {
            Row(
                modifier = cardModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gambar (jika ada)
                config.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Card Image",
                        modifier = Modifier
                            .width(config.imageHeight) // Menggunakan imageHeight sebagai lebar untuk horizontal
                            .height(config.imageHeight)
                            .clip(RoundedCornerShape(topStart = config.cornerRadius, bottomStart = config.cornerRadius)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Konten teks
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(config.padding)
                ) {
                    config.title?.let {
                        Text(
                            text = it,
                            style = config.titleStyle
                        )
                    }
                    config.subtitle?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = config.subtitleStyle
                        )
                    }
                    config.actionText?.let { action ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = action,
                            style = config.actionStyle,
                            modifier = Modifier
                                .clickable { config.onActionClick?.invoke() }
                        )
                    }
                }
            }
        }
    }
}