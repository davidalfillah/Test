package com.example.test.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.test.ui.viewModels.Donation
import com.example.test.ui.viewModels.DonationViewModel
import java.text.NumberFormat
import java.util.Locale


data class Category(
    val name: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    onGeneralDonationClick: (Long) -> Unit,
    onCharityDonationClick: (Donation, Long) -> Unit,
    viewModel: DonationViewModel = viewModel() // Injeksi ViewModel
) {
    val context = LocalContext.current
    val categories = listOf(
        Category("Umum", Icons.Default.Notifications),
        Category("Zakat", Icons.Default.DateRange),
        Category("Sosial", Icons.Default.Notifications)
    )

    var donations by remember { mutableStateOf(emptyList<Donation>()) }

    LaunchedEffect(Unit) {
        viewModel.fetchDonations("Semua",
            onSuccess = { donationsList -> donations = donationsList },
            onFailure = { errorMessage ->
                Toast.makeText(context, "Gagal memuat donasi: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donasi") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    titleContentColor = MaterialTheme.colorScheme.onTertiary
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
                
//                actions = {
//                    AddDonationButton(
//                        viewModel = DonationViewModel(),
//                        navController = navController
//                    )
//                }
                
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                )
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bersama Kita Wujudkan Kebaikan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Kategori
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                categories.forEach { category ->
                    CategoryButton(category = category, onClick = { })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (donations.isEmpty()) {
                Text(
                    text = "Tidak ada donasi tersedia",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    items(donations) { donation ->
                        CharityDonationCarouselItem(charity = donation, onClick = {
                            navController.navigate("donation_detail/${donation.id}")
                        })
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    donations.forEach({ item ->
                        CharityDonationListItem(charity = item, onClick = {
                            navController.navigate("donation_detail/${item.id}")
                        })
                    })
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    items(donations) { donation ->
                        CharityDonationCarouselItem(charity = donation, onClick = {
                            navController.navigate("donation_detail/${donation.id}")
                        })
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    donations.forEach({ item ->
                        CharityDonationListItem(charity = item, onClick = {
                            navController.navigate("donation_detail/${item.id}")
                        })
                    })
                }

            }
        }

    }
}

@Composable
fun CategoryButton(category: Category, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(54.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CharityDonationCarouselItem(charity: Donation, onClick: () -> Unit) {
    val formattedTotalCollected = NumberFormat.getNumberInstance(Locale("id", "ID")).format(charity.totalCollected)
    val formattedTargetAmount = charity.targetAmount?.let {
        NumberFormat.getNumberInstance(Locale("id", "ID")).format(it)
    } ?: "-"

    Box(
        modifier = Modifier
            .width(147.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(color = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column {
            AsyncImage(
                model = charity.thumbnailUrl,
                contentDescription = "Gambar Proyek",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = charity.title,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                val progress = (charity.totalCollected.toFloat() / (charity.targetAmount?.toFloat() ?: 1f))
                    .coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        trackColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Rp $formattedTotalCollected / $formattedTargetAmount",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CharityDonationListItem(charity: Donation, onClick: () -> Unit) {
    // Format angka untuk IDR
    val formattedTotalCollected = NumberFormat.getNumberInstance(Locale("id", "ID")).format(charity.totalCollected)
    val formattedTargetAmount = charity.targetAmount?.let {
        NumberFormat.getNumberInstance(Locale("id", "ID")).format(it)
    } ?: "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top // Sesuaikan tinggi elemen
    ) {
        // Gambar di sebelah kiri
        AsyncImage(
            model = charity.thumbnailUrl,
            contentDescription = "Gambar Proyek",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))
        // Konten teks dan progress di sebelah kanan
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = charity.title,
                fontSize = 14.sp, // Sedikit lebih besar untuk list view
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            val progress = (charity.totalCollected.toFloat() / (charity.targetAmount?.toFloat() ?: 1f))
                .coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    trackColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Rp $formattedTotalCollected / $formattedTargetAmount",
                fontSize = 12.sp, // Sedikit lebih besar dari carousel
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

//@Composable
//fun AddDonationButton(viewModel: DonationViewModel, navController: NavController) {
//    var isLoading by remember { mutableStateOf(false) }
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Button(
//            onClick = {
//                isLoading = true
//                val newDonation = Donation(
//                    id = "",
//                    title = "Waspadai Kecelakaan, Proteksi Dirimu Sekarang!",
//                    thumbnailUrl = "https://campaign.salingjaga.com/_next/image?url=https%3A%2F%2Fimgix.kitabisa.com%2Fe660be06-5216-4177-8fc1-3bb4bfd2508e.jpg",
//                    category = "Zakat",
//                    content = listOf(
//                        ContentItem(
//                            type = "text",
//                            value = "Sayangnya, kebahagiaan berbuka puasa dan berkumpul bersama keluarga masih menjadi impian bagi saudara-saudara kita. Di antara mereka, ada santri yang jauh dari orang tua, lansia dhuafa yang hidup dalam keterbatasan, dan pejuang keluarga yang berjuang mencari nafkah hingga lupa memikirkan diri sendiri."
//                        ),
//                        ContentItem(
//                            type = "image",
//                            value = "https://imgix.kitabisa.com/f934e44f-e8a1-11ef-8417-96f158cd9026_E753DE426B961D21.jpg"
//                        ),
//                        ContentItem(
//                            type = "text",
//                            value = "Di bulan penuh berkah ini, mari kita hadirkan kebahagiaan bagi mereka dengan berbagi iftar dan sahur. Satu porsi yang kita berikan, bukan hanya sekadar makanan, tetapi juga bentuk kasih sayang dan kepedulian."
//                        ),
//                        ContentItem(
//                            type = "image",
//                            value = "https://imgix.kitabisa.com/7e7d6026-e8a2-11ef-8417-96f158cd9026_D29C90C23D0C07E.jpg"
//                        ),
//                        ContentItem(
//                            type = "text",
//                            value = "Mari bersama-sama menebarkan kebaikan di bulan yang penuh suci dan berkah ini. Winakara Foundation bersama Kitabisa ingin mengajak #OrangBaik semua untuk memperbanyak pahala di Bulan Ramadhan dengan membagikan paket makanan berbuka puasa dan sahur untuk yang membutuhkan."
//                        ),
//                    ),
//                    totalCollected = 0,
//                    targetAmount = 100000,
//                    deadline = Timestamp.now()
//                )
//
//                viewModel.addDonation(newDonation,
//                    onSuccess = {
//                        isLoading = false
//                        coroutineScope.launch {
//                            snackbarHostState.showSnackbar("Donasi berhasil ditambahkan!")
//                        }
//                    },
//                    onFailure = { errorMessage ->
//                        isLoading = false
//                        coroutineScope.launch {
//                            snackbarHostState.showSnackbar("Gagal menambahkan donasi: $errorMessage")
//                        }
//                    }
//                )
//            },
//            enabled = !isLoading
//        ) {
//            if (isLoading) {
//                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
//            } else {
//                Text("Tambah Donasi")
//            }
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        SnackbarHost(hostState = snackbarHostState)
//    }
//}
