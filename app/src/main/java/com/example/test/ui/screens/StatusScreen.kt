package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.test.ui.components.SlideComponentBanner
import com.example.test.ui.components.SlideComponentNews
import com.example.test.ui.dataTest.banners

@Composable
fun StatusScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = "Anda Sudah terdaftar sebagai Anggota Organisasi Gerakan Rakyat Indonesia Bersatu (GRIB) Jaya")
    }
}