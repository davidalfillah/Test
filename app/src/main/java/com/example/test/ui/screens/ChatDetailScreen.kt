package com.example.test.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.navigation.NavHostController
import com.example.test.AuthViewModel
import com.example.test.R
import com.example.test.ui.components.UserProfileImage
import com.example.test.ui.components.formatTimeAgo
import com.example.test.ui.dataType.ChatData
import com.example.test.ui.dataType.ChatUserData
import com.example.test.ui.dataType.Message
import com.google.firebase.Timestamp
import com.example.test.ui.viewModels.ChatViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavHostController,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    chatId: String,  // ID chat yang sedang dibuka
) {
    val user by authViewModel.user.collectAsState()
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    val messageText = remember { mutableStateOf("") }
    var chatUser by remember { mutableStateOf(ChatUserData(name = "Loading...", imageUrl = "")) }
    val listState = rememberLazyListState()


    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1) // Auto-scroll ke pesan terbaru
        }

    }

    Log.d("ChatDetailScreen", "chatUser: $chatUser")



    LaunchedEffect(chatId) {
        launch {
            user?.let {
                Log.d("ChatScreen", "Resetting unread messages for chatId: $chatId, userId: ${it.uid}")
                chatViewModel.resetUnreadMessages(chatId, it.uid)
            }
        }

        launch {
            chatViewModel.listenToMessages(chatId) { messagesData ->
                messages = messagesData
                Log.d("ChatScreen", "Received ${messagesData.size} messages")
            }
        }

        launch {
            chatViewModel.getChatUser(chatId) { userData ->
                chatUser = userData
                Log.d("ChatScreen", "Chat user data loaded: ${userData.name}")
            }
        }

        launch {
            user?.let {
                Log.d("ChatScreen", "Listening for unread updates for chatId: $chatId, userId: ${it.uid}")
                chatViewModel.listenToUnreadUpdates(chatId, it.uid)
            }
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserProfileImage(chatUser.imageUrl, 40)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(chatUser.name)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { /* Aksi ketika tombol pencarian ditekan */ }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_more_vert_24),
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            ChatInputField(
                messageText = messageText.value,
                onMessageChange = { messageText.value = it },
                onSendMessage = {
                    if (messageText.value.isNotEmpty()) {
                        user?.let {
                            chatViewModel.sendMessage(
                                chatId,
                                messageText.value,
                                it.uid,
                            )
                        }
                        messageText.value = ""
                    }
                }
            )
        }
    ) { paddingValues ->
        MessageList(messages, user?.uid, paddingValues, listState)
    }

}




@Composable
fun ChatInputField(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White, shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = messageText,
            onValueChange = onMessageChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ketik pesan...") },
            maxLines = 3
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSendMessage) {
            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Blue)
        }
    }
}



@Composable
fun MessageBubble(message: Message, isUserMessage: Boolean) {
    val alignment = if (isUserMessage) Alignment.End else Alignment.Start
    val bubbleColor = if (isUserMessage) Color.Blue else Color.Gray

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(bubbleColor, shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Column {
                Text(text = message.content, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimeAgo(message.time),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}


@Composable()
fun MessageList(messages: List<Message>, currentUserId: String?, paddingValues: PaddingValues, listState: LazyListState = rememberLazyListState()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        state = listState, // Gunakan listState untuk kontrol scroll
        reverseLayout = false // Pastikan tidak dibalik
    ) {
        items(messages) { message ->
            MessageBubble(message, isUserMessage = message.senderId == currentUserId)
        }
    }
}
