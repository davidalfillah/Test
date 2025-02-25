package com.example.test.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.test.R

@Composable
fun UserProfileImage(profilePicUrl: String?, size: Int = 80) {
    if (profilePicUrl.isNullOrEmpty()) {
        Image(
            painter = painterResource(id = R.drawable.baseline_person_24),
            contentDescription = "Default Profile",
            colorFilter = ColorFilter.tint(Color.Gray),
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Color.LightGray) // Warna lebih halus
        )
    } else {
        AsyncImage(
            model = profilePicUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}