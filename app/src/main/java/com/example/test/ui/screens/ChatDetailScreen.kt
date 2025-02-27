package com.example.test.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.TextField
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.test.AuthViewModel
import com.example.test.R
import com.example.test.ui.components.ChatBubble
import com.example.test.ui.components.ChatInputField
import com.example.test.ui.components.TypingIndicator
import com.example.test.ui.components.UserProfileImage
import com.example.test.ui.components.formatTimeAgo
import com.example.test.ui.viewModels.ChatViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch


data class UserInfo(
    val id: String,
    val name: String,
    val imageUrl: String,
    val isOnline: Boolean,
    val lastSeen: Timestamp?,
    val isGroup: Boolean
)

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavHostController,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    chatId: String,  // ID chat yang sedang dibuka
) {
    val user by authViewModel.user.collectAsState()
    val messages by chatViewModel.getMessages(chatId, user!!.uid).collectAsStateWithLifecycle(initialValue = emptyList())
    val chatInfo by chatViewModel.getChatInfo(chatId).collectAsState(initial = null) // Info chat
    val isOtherUserTyping by chatViewModel.isTyping(chatId, user!!.uid).collectAsState(initial = false)

    val otherUser = if (chatInfo?.isGroup == true) {
        UserInfo(
            id = chatInfo!!.chatId,
            name = chatInfo!!.groupName ?: "",
            imageUrl = chatInfo!!.groupImageUrl ?: "",
            isOnline = false,
            lastSeen = null,
            isGroup = true
        )
    } else {
        val user = chatInfo?.participantsInfo?.values?.firstOrNull { it?.uid != user?.uid }

        UserInfo(
            id = user?.uid ?: "",
            name = user?.name ?: "",
            imageUrl = user?.profilePicUrl ?: "",
            isOnline = user?.isOnline ?: false,
            lastSeen = user?.lastSeen,
            isGroup = false
        )
    }


    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope() // Tambahkan Coroutine Scope
    var isInitialLoad by remember { mutableStateOf(true) } // Tambahan flag untuk membedakan initial load dan pagination
    var isFabVisible by remember { mutableStateOf(false) } // State untuk menampilkan FAB
    var isLoadingMore by remember { mutableStateOf(false) } // State untuk indikator loading


    Log.d("ChatDetailScreen", chatInfo.toString())

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
                        UserProfileImage(otherUser.imageUrl, 40)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                otherUser.name ?: "Loading",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                lineHeight = 18.sp
                            )
                            Log.d("user", otherUser.toString())
                            if (otherUser.isOnline) { // ✅ Pastikan cek dengan `== true`
                                Text(
                                    text = "Online",
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp
                                )
                            } else if (otherUser.lastSeen != null) { // Jika offline, tampilkan last seen
                                Text(
                                    text = "Terakhir terlihat: ${formatTimeAgo(otherUser.lastSeen)}",
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
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
            user?.let { chatInfo?.let { it1 -> ChatInputField(chatId, chatViewModel, it, participants = it1.participants) } }
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding()) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxSize(),
                reverseLayout = true,
                contentPadding = PaddingValues(horizontal = 6.dp),
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


