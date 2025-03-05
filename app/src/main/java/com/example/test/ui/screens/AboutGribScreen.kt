package com.example.test.ui.screens

import android.graphics.Color.alpha
import android.os.Looper.prepare
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.test.R
import com.example.test.ui.components.formatNewsContent


data class ContentItem(
    val type: String,  // "text", "image", "video"
    val value: String  // Isi teks, URL gambar, atau URL video
)

val aboutContent = listOf(
    ContentItem("image", "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEhuN3iKdunuG0uNVvXu9AuT0s8HfK-O7zDytNKuKvMmv2H_pUpPXW7KKG7FIWA9TcqlH6QjNOaCtm-L3sr4FQu92A6VPFLtELXjPN3x1IjRixtDmLvZ8ofeNXeGr9hzrQjYyTm5HyrNDFU/s1600/LOGO+GRIB.jpg"),
    ContentItem("text", "Profil Singkat **Gerakan Rakyat Indonesia Bersatu (GRIB)**"),
    ContentItem("text", "Didirikan pada 2011 oleh **Hercules Rosario Marshal**, GRIB adalah ormas yang dikenal memiliki basis massa yang cukup besar."),
    ContentItem("text", "Organisasi ini memiliki tujuan untuk memperjuangkan hak-hak rakyat melalui sinergi dan kegiatan berbasis kemasyarakatan."),
    ContentItem("text", "GRIB yang dipimpin oleh Hercules, seorang mantan preman yang kini beralih menjadi pebisnis, berfokus pada pembelaan hak rakyat dan mendukung kejujuran, partisipasi, dan keadilan."),
    ContentItem("text", "Organisasi ini mengklaim memiliki legalitas resmi dan sudah memiliki cabang di berbagai wilayah di Indonesia, termasuk sekitar 750 anggota di Blora."),
    ContentItem("video", "https://www.youtube.com/watch?v=MZW6F_UAaFo"),
)

val pengurusContent = listOf(
    ContentItem("image", "https://wartalika.id/wp-content/uploads/2022/02/Hercules-Grib.jpg"),
    ContentItem("text", "**Hercules Rosario Marshal**" + "\n" + "Ketua Umum"),
    ContentItem("text", "GRIB yang dipimpin oleh Hercules, seorang mantan preman yang kini beralih menjadi pebisnis, berfokus pada pembelaan hak rakyat dan mendukung kejujuran, partisipasi, dan keadilan.Organisasi ini mengklaim memiliki legalitas resmi dan sudah memiliki cabang di berbagai wilayah di Indonesia, termasuk sekitar 750 anggota di Blora."),
    ContentItem("image", "https://beritamoneter.com/wp-content/uploads/2024/11/Anan-Wijaya.jpg"),
    ContentItem("text", "**Anan Wijaya**" + "\n" + "Ketua Harian"),
    ContentItem("text", "GRIB yang dipimpin oleh Hercules, seorang mantan preman yang kini beralih menjadi pebisnis, berfokus pada pembelaan hak rakyat dan mendukung kejujuran, partisipasi, dan keadilan.Organisasi ini mengklaim memiliki legalitas resmi dan sudah memiliki cabang di berbagai wilayah di Indonesia, termasuk sekitar 750 anggota di Blora."),
    ContentItem("image", "https://scontent.fcgk47-1.fna.fbcdn.net/v/t39.30808-6/469811191_599285909145985_3676644724033676951_n.jpg?stp=dst-jpg_p552x414_tt6&_nc_cat=101&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeEqGiOf8Xx4URb8fWQcD9NSsCeIL4Px_1OwJ4gvg_H_UzUcJ_Cu2K4Djuihb5K5TU45eMtafYM3QEIoBvgNgLXS&_nc_ohc=SiQsPExAXPwQ7kNvgFjT-Ty&_nc_oc=AdgGzdpEXgVOng_oQRgBDihbL4CIYDWmwpJJTYQgKon8cl69F85uV9cAot-pf4ZXA_Q&_nc_zt=23&_nc_ht=scontent.fcgk47-1.fna&_nc_gid=ACxFRtzWF1yToJUhJRt4S9E&oh=00_AYDeGraZlvazE7UQv_LaWOS8iTc6WJJ0V-DH4oQfED4HQA&oe=67CD76BE"),
    ContentItem("text", "**H. Zulfikar, SE. GG**" + "\n" + "Sekretaris Jenderal"),
    ContentItem("text", "GRIB yang dipimpin oleh Hercules, seorang mantan preman yang kini beralih menjadi pebisnis, berfokus pada pembelaan hak rakyat dan mendukung kejujuran, partisipasi, dan keadilan.Organisasi ini mengklaim memiliki legalitas resmi dan sudah memiliki cabang di berbagai wilayah di Indonesia, termasuk sekitar 750 anggota di Blora."),
)





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutGribScreen(navController: NavHostController) {
    var selectedIndex by remember { mutableStateOf(0) }
    val list = listOf("Hello GRIB", "Pengurus Pusat")

    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Tentang",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )  },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            )

        }
    ){ paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
                TabRow(selectedTabIndex = selectedIndex,
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(50)),
                    indicator = { tabPositions: List<TabPosition> ->
                        Box(){}
                    },
                    containerColor = MaterialTheme.colorScheme.primary,

                    ) {
                    list.forEachIndexed { index, text ->
                        val selected = selectedIndex == index
                        Tab(
                            modifier = if (selected) Modifier
                                .padding(2.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Color.White
                                )
                            else Modifier
                                .padding(2.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    MaterialTheme.colorScheme.primary
                                ),
                            selected = selected,
                            onClick = { selectedIndex = index },
                            text = { Text(text = text, color = if(selected){
                                MaterialTheme.colorScheme.primary
                            }else{
                                Color.White
                            }) }
                        )
                    }
                }

            if (selectedIndex == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                ) {
                    items(aboutContent) { contentItem ->
                        when (contentItem.type) {
                            "text" -> FormattedText(contentItem.value)
                            "image" -> AsyncImage(
                                model = contentItem.value,
                                contentDescription = "Gambar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            "video" -> VideoPlayer(contentItem.value)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }else{
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                ) {
                    items(pengurusContent) { contentItem ->
                        when (contentItem.type) {
                            "text" -> FormattedText(contentItem.value)
                            "image" -> AsyncImage(
                                model = contentItem.value,
                                contentDescription = "Gambar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            "video" -> VideoPlayer(contentItem.value)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun FormattedText(text: String) {
    val annotatedText = buildAnnotatedString {
        var currentIndex = 0
        val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()
        val italicRegex = "\\*(.*?)\\*".toRegex()
        val colorRegex = "\\{(.*?)\\}".toRegex() // Contoh: {merah}

        val matches = (boldRegex.findAll(text) + italicRegex.findAll(text) + colorRegex.findAll(text))
            .sortedBy { it.range.first }

        if (matches is List<*> && matches.isEmpty()) {
            append(text) // Jika tidak ada format, tampilkan biasa
            return@buildAnnotatedString
        }

        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1

            // Hindari IndexOutOfBoundsException
            if (start < currentIndex) continue

            if (currentIndex < start) {
                append(text.substring(currentIndex, start))
            }

            val isBold = match.value.startsWith("**")
            val isItalic = match.value.startsWith("*")
            val isColor = match.value.startsWith("{")

            val cleanText = when {
                isBold -> match.value.removeSurrounding("**")
                isItalic -> match.value.removeSurrounding("*")
                isColor -> match.value.removeSurrounding("{", "}")
                else -> match.value
            }

            withStyle(
                style = when {
                    isBold -> SpanStyle(fontWeight = FontWeight.Bold)
                    isItalic -> SpanStyle(fontStyle = FontStyle.Italic)
                    isColor -> SpanStyle(color = Color.Red) // Bisa ubah warna sesuai kebutuhan
                    else -> SpanStyle()
                }
            ) {
                append(cleanText)
            }

            currentIndex = end
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex, text.length))
        }
    }

    Text(text = annotatedText, fontSize = 16.sp)
}


@Composable
fun VideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

