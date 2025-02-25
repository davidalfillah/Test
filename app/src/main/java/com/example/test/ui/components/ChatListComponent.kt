package com.example.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.test.R
import com.example.test.ui.dataTest.ChatItem



@Composable
fun ChatItemComponent(chat: ChatItem, onClick: (ChatItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(chat) }
            .padding(horizontal = 16.dp).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture + Nama + Pesan Terakhir
        Row(
            modifier = Modifier.weight(1f), // Supaya teks tidak melebihi batas kanan
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserProfileImage(chat.profilePic, 40)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = chat.name, fontWeight = FontWeight.Bold)
                Text(text = chat.lastMessage, fontSize = 14.sp, color = Color.Gray)
            }
        }

        // Waktu + Badge
        Column(
            modifier = Modifier.fillMaxHeight(), // Supaya tetap berada di atas
            horizontalAlignment = Alignment.End
        ) {
            Text(text = chat.time, fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(4.dp)) // Jarak antara waktu dan badge

            if (chat.badge > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.Red, shape = CircleShape)
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text(
                        text = chat.badge.toString(),
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


