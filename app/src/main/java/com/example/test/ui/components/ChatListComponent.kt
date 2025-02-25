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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.test.R
import com.example.test.ui.dataTest.ChatItem
import com.example.test.ui.dataType.ChatData
import com.example.test.ui.dataType.ChatUserData
import com.example.test.ui.dataType.Message
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ChatItemComponent(chat: ChatUserData, message: Message, onClick: (ChatUserData) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(chat) } // ✅ Memanggil fungsi onClick
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture + Nama + Pesan Terakhir
        Row(
            modifier = Modifier.weight(1f), // Supaya teks tidak melebihi batas kanan
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserProfileImage(chat.imageUrl, 60)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = chat.name, fontWeight = FontWeight.Bold)
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1, // ✅ Agar teks tidak terlalu panjang
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Waktu + Badge
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatTimeAgo(message.time), // ✅ Format waktu
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp)) // Jarak antara waktu dan badge

            if (chat.unread > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.Red, shape = CircleShape)
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text(
                        text = chat.unread.toString(),
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


fun formatTimeAgo(timestamp: Timestamp?): String {
    if (timestamp == null) return ""

    val date = timestamp.toDate()
    val now = Date()
    val diff = now.time - date.time

    return when {
        diff < 60_000 -> "Baru saja"
        diff < 3_600_000 -> "${diff / 60_000} menit lalu"
        diff < 86_400_000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
    }
}



