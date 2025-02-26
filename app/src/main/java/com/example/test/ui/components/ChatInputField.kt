package com.example.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.screens.User
import com.example.test.ui.viewModels.ChatViewModel
@Composable
fun ChatInputField(chatId: String, chatViewModel: ChatViewModel, user: User) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Input Field dalam Card untuk tampilan lebih rapi
        Card(
            modifier = Modifier
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            TextField(
                value = text,
                onValueChange = { newValue ->
                    text = newValue
                    chatViewModel.updateTypingStatus(chatId, newValue.isNotEmpty(), user!!)
                },
                modifier = Modifier
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            chatViewModel.updateTypingStatus(chatId, false, user!!)
                        }
                    }.fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 0.dp),
                placeholder = { Text("Ketik pesan...") },
                maxLines = 3,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(fontSize = 16.sp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Tombol Kirim
        IconButton(
            onClick = {
                chatViewModel.sendMessage(
                    chatId = chatId,
                    senderId = user.uid ?: "",
                    text = text,
                    mediaUrl = "",
                    mediaType = ""
                )
                text = ""
            },
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
        ) {
            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
        }
    }
}



