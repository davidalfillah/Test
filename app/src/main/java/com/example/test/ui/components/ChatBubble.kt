package com.example.test.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.dataType.Message

@Composable
fun ChatBubble(message: Message, isMe: Boolean) {
    val bubbleColor = if (isMe) MaterialTheme.colorScheme.surfaceVariant else Color.White
    val textColor = if (isMe) Color.Black else Color.Black
    val statusIcon = when {
        message.unreadBy.isEmpty() -> R.drawable.baseline_done_all_24  // ✅ Semua sudah membaca (✔✔)
        else -> R.drawable.baseline_done_all_24  // ⏳ Masih ada yang belum membaca (✔)
    }



    val cornerShape = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp,
        bottomStart = if (isMe) 12.dp else 0.dp,
        bottomEnd = if (isMe) 0.dp else 12.dp,
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .background(bubbleColor, shape = cornerShape)
                .padding(12.dp)
        ) {
            message.text?.let { Text(it, color = textColor) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                        formatTimeAgo(message.timestamp),
                        color = textColor.copy(0.7f),
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                    )
                if(isMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = ImageVector.vectorResource(statusIcon),
                        contentDescription = "Message Status",
                        tint = if (message.unreadBy.isEmpty()) Color.Blue else textColor.copy(0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}