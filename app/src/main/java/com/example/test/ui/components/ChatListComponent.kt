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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.test.R
import com.example.test.ui.dataTest.ChatItem
import com.example.test.ui.dataType.Chat
import com.example.test.ui.dataType.Message
import com.example.test.ui.viewModels.ChatViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ChatItemComponent(chat: Chat, onClick: (Chat) -> Unit, otherUserProfileUrl: String?, otherUserId: String?, userUid: String?, chatViewModel: ChatViewModel) {
    val name = if (chat.isGroup) chat.groupName ?: "Grup Tanpa Nama" else otherUserId ?: "Unknown"

    val otherUser = chat.participantsInfo.values.firstOrNull { it?.uid != userUid }

    val statusIcon = when {
        chat.lastUnreadBy.isEmpty() -> R.drawable.baseline_done_all_24  // ✅ Semua sudah membaca (✔✔)
        else -> R.drawable.baseline_done_all_24  // ⏳ Masih ada yang belum membaca (✔)
    }

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
            UserProfileImage(otherUser?.profilePicUrl,60
            )

            Spacer(modifier = Modifier.width(12.dp))
            Column {
                otherUser?.name?.let { Text(text = it, fontWeight = FontWeight.Bold) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if(chat.lastSenderId == userUid){
                        Icon(
                            imageVector = ImageVector.vectorResource(statusIcon),
                            contentDescription = "Message Status",
                            tint = if (chat.lastUnreadBy.isEmpty()) Color.Blue else Color.Gray.copy(0.7f),
                            modifier = Modifier.size(14.dp)
                        )

                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = chat.lastMessage,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 1, // ✅ Agar teks tidak terlalu panjang
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Waktu + Badge
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.End
        ) {

            Text(
                text = formatTimeAgo(chat.lastMessageTimestamp), // ✅ Format waktu
                fontSize = 12.sp,
                color = if(chat.unreadCount[userUid]!! > 0 ) {
                    MaterialTheme.colorScheme.primary
                }else{
                    Color.Gray
                }
            )

            Spacer(modifier = Modifier.height(4.dp)) // Jarak antara waktu dan badge

            if (chat.lastSenderId != userUid && chat.unreadCount[userUid]!! > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.Red, shape = CircleShape)
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text(
                        text = chat.unreadCount[userUid].toString(),
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





