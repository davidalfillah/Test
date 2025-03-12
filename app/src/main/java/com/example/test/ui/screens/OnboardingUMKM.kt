package com.example.test.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingUMKMScreen(
    onNavigateToRegistration: () -> Unit,
    onFinishOnboarding: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            "Selamat Datang di UMKM",
            "UMKM adalah Usaha Mikro, Kecil, dan Menengah yang menjadi penggerak ekonomi rakyat Indonesia"
        ),
        OnboardingPage(
            "Pengertian UMKM",
            "Usaha produktif milik perorangan atau badan usaha yang memenuhi kriteria sebagai usaha mikro sesuai UU No.20 tahun 2008"
        ),
        OnboardingPage(
            "Mari Mulai!",
            "Bergabunglah dengan jutaan pelaku UMKM lainnya dan kembangkan bisnis Anda"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope() // Tambahkan ini untuk handling animasi

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { position ->
            OnboardingPage(pages[position])
        }
        
        // Indicators
        Row(
            Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                }
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(8.dp),
                    content = { Surface(color = color, shape = MaterialTheme.shapes.small) {} }
                )
            }
        }

        // Tambahkan Button navigasi
        Button(
            onClick = {
                if (pagerState.currentPage < pages.size - 1) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onNavigateToRegistration()
                    onFinishOnboarding()
                }
            },
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = if (pagerState.currentPage < pages.size - 1) "Selanjutnya" else "Mulai Registrasi"
            )
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}