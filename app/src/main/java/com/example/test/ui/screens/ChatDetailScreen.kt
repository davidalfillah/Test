package com.example.test.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.example.test.AuthViewModel
import com.example.test.R
import com.example.test.ui.components.ChatBubble
import com.example.test.ui.components.ChatInputField
import com.example.test.ui.components.TypingIndicator
import com.example.test.ui.components.UserProfileImage
import com.example.test.ui.components.formatTimeAgo
import com.example.test.ui.dataType.Chat
import com.example.test.ui.dataType.Message
import com.example.test.ui.viewModels.ChatViewModel
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
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
    val messages by chatViewModel.getMessages(chatId).collectAsState(initial = emptyList()) // Pesan chat
    val chatInfo by chatViewModel.getChatInfo(chatId).collectAsState(initial = null) // Info chat
    val isOtherUserTyping by chatViewModel.isTyping(chatId, user!!.uid).collectAsState(initial = false)

    val otherUser = if (chatInfo?.isGroup == true) {
        Triple(chatInfo!!.chatId, chatInfo!!.groupName ?: "", chatInfo!!.groupImageUrl ?: "")
    } else {
        val user = chatInfo?.participantsInfo?.values?.firstOrNull { it?.uid != user?.uid }
        Triple(user?.uid ?: "", user?.name ?: "", user?.profilePicUrl ?: "")
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope() // Tambahkan Coroutine Scope
    var isInitialLoad by remember { mutableStateOf(true) } // Tambahan flag untuk membedakan initial load dan pagination
    var isFabVisible by remember { mutableStateOf(false) } // State untuk menampilkan FAB
    var isLoadingMore by remember { mutableStateOf(false) } // State untuk indikator loading




    Scaffold(
        modifier = Modifier
            .consumeWindowInsets(PaddingValues()) // Menyerap insets agar TopAppBar tidak ikut naik
            .windowInsetsPadding(WindowInsets.ime), // Mencegah seluruh layar naik

        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserProfileImage(otherUser.third, 40)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(otherUser.second)
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
//            ChatInputField(
//                modifier = Modifier.imePadding(),
//                messageText = messageText.value,
//                onMessageChange = { messageText.value = it.replace("\n", "") },
//                onSendMessage = {
//                    if (messageText.value.isNotEmpty()) {
//                        user?.let {
//                            chatViewModel.sendMessage(
//                                chatId,
//                                messageText.value,
//                                it,
//                            )
//                        }
//                        messageText.value = ""
//                    }
//                }
//            )


            user?.let { ChatInputField(chatId, chatViewModel, it) }
        },
        floatingActionButton = {
            if (isFabVisible) { // FAB hanya muncul saat pengguna scroll ke atas
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.lastIndex)
                        }
                    },
                    shape = CircleShape, // Membuat FAB tetap bulat
                    modifier = Modifier
                        .size(44.dp) // FAB lebih kecil dari default (56.dp)
                        .offset(x = (8).dp, y = (16).dp) // Lebih dekat ke pojok kanan bawah
                        .padding(end = 12.dp, bottom = 12.dp), // Memberi jarak agar tidak menempel ke tepi
                    containerColor = Color.White.copy(alpha = 0.8f), // Warna putih dengan transparansi
                    elevation = FloatingActionButtonDefaults.elevation(1.dp) // Elevasi lebih kecil agar tidak terlalu timbul
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll Down",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp) // Mengecilkan ikon agar proporsional
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End


    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = true
            ) {
                items(messages) { message ->
                    ChatBubble(message, isMe = message.senderId == user?.uid)
                }
            }

            if (isOtherUserTyping) {
                TypingIndicator()
            }
        }

    }

}




//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ChatInputField(
//    modifier: Modifier = Modifier,
//    messageText: String,
//    onMessageChange: (String) -> Unit,
//    onSendMessage: () -> Unit
//) {
//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(horizontal = 8.dp, vertical = 8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Input Field dalam Card untuk tampilan lebih rapi
//        Card(
//            modifier = Modifier
//                .weight(1f),
//            shape = RoundedCornerShape(24.dp),
//            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer),
//            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
//        ) {
//            TextField(
//                value = messageText,
//                onValueChange = onMessageChange,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 8.dp, vertical = 0.dp),
//                placeholder = { Text("Ketik pesan...") },
//                maxLines = 3,
//                colors = TextFieldDefaults.colors(
//                    unfocusedContainerColor = Color.Transparent,
//                    focusedContainerColor = Color.Transparent,
//                    disabledContainerColor = Color.Transparent,
//                    unfocusedIndicatorColor = Color.Transparent,
//                    focusedIndicatorColor = Color.Transparent,
//                    disabledIndicatorColor = Color.Transparent
//                ),
//                textStyle = TextStyle(fontSize = 16.sp)
//            )
//        }
//
//        Spacer(modifier = Modifier.width(8.dp))
//
//        // Tombol Kirim
//        IconButton(
//            onClick = onSendMessage,
//            modifier = Modifier
//                .size(48.dp)
//                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
//        ) {
//            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
//        }
//    }
//}
//
//
//
//
//class TriangleEdgeShape(private val isUserMessage: Boolean) {
//    fun drawTriangle(size: androidx.compose.ui.geometry.Size): Path {
//        return Path().apply {
//            val triangleSize = 16f // Ukuran ekor runcing
//
//            if (isUserMessage) {
//                moveTo(size.width.toFloat(), size.height - triangleSize) // Mulai dari kanan bawah
//                lineTo(size.width.toFloat(), size.height.toFloat()) // Turun ke bawah
//                lineTo(size.width - triangleSize, size.height.toFloat()) // Geser ke kiri untuk ujung
//                close()
//            } else {
//                moveTo(0f, size.height - triangleSize) // Mulai dari kiri bawah
//                lineTo(0f, size.height.toFloat()) // Turun ke bawah
//                lineTo(triangleSize, size.height.toFloat()) // Geser ke kanan untuk ujung
//                close()
//            }
//        }
//    }
//}

//@Composable
//fun MessageBubble(message: Message, isUserMessage: Boolean) {
//    val bubbleColor = if (isUserMessage) Color(0xFF007AFF) else Color(0xFFE5E5EA)
//    val textColor = if (isUserMessage) Color.White else Color.Black
//    val cornerShape = RoundedCornerShape(
//        topStart = 18.dp,
//        topEnd = 18.dp,
//        bottomStart = if (isUserMessage) 18.dp else 0.dp,
//        bottomEnd = if (isUserMessage) 0.dp else 18.dp
//    )
//
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
//    ) {
//        Box(
//            modifier = Modifier
//                .padding(horizontal = 8.dp, vertical = 4.dp)
//                .widthIn(max = 320.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .drawBehind {
//                        drawPath(
//                            path = TriangleEdgeShape(isUserMessage).drawTriangle(size),
//                            color = bubbleColor
//                        )
//                    }
//                    .background(bubbleColor, shape = cornerShape)
//                    .padding(horizontal = 14.dp, vertical = 10.dp)
//            ) {
//                Column {
//
//
//                    Text(
//                        text = if (message.status == "recalled") "Pesan telah ditarik" else message.content,
//                        fontStyle = if (message.status == "recalled") FontStyle.Italic else FontStyle.Normal,
//                        color = textColor,
//                        fontSize = 16.sp,
//                        lineHeight = 20.sp
//                    )
//                    Spacer(modifier = Modifier.height(6.dp))
//                    Row(
//                        modifier = Modifier.align(Alignment.End),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = formatTimeAgo(message.time),
//                            fontSize = 12.sp,
//                            color = textColor.copy(alpha = 0.6f)
//                        )
//
//                        Spacer(modifier = Modifier.width(4.dp))
//                        if (isUserMessage) {
//                            MessageStatusIcon(message.status)
//                        }
//                    }
//
//                }
//            }
//        }
//    }
//    Spacer(modifier = Modifier.height(6.dp))
//}
//
//
//
//@Composable
//fun MessageStatusIcon(status: String) {
//    when (status) {
//        "pending" -> Icon(Icons.Default.DateRange, contentDescription = "Pending")  // ⏳
//        "sent" -> Icon(Icons.Default.Check, contentDescription = "Sent")  // ✔
//        "delivered" -> Icon(Icons.Default.CheckCircle, contentDescription = "Delivered")  // ✔✔
//        "read" -> Icon(Icons.Default.CheckCircle, tint = Color.Blue, contentDescription = "Read")  // ✔✔ (biru)
//        "failed" -> Icon(Icons.Default.Clear, contentDescription = "Failed", tint = Color.Red)  // ❌
//    }
//}
//
//
//
//
//@Composable()
//fun MessageList(messages: List<Message>, currentUserId: String?, paddingValues: PaddingValues, listState: LazyListState = rememberLazyListState(), isLoadingMore: Boolean = false) {
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(paddingValues)
//            .padding(horizontal = 16.dp),
//        state = listState, // Gunakan listState untuk kontrol scroll
//        reverseLayout = false // Pastikan tidak dibalik
//    ) {
//        // Indikator loading pagination di bagian atas
//        item {
//            if (isLoadingMore) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(24.dp),
//                        strokeWidth = 2.dp
//                    )
//                }
//            }
//        }
////        items(messages, key = { it.msgId }) { message ->
////            MessageBubble(message, isUserMessage = message.senderId == currentUserId)
////        }
//    }
//
//}
