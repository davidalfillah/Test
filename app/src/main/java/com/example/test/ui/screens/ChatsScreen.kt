package com.example.test.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.test.AuthViewModel
import com.example.test.R
import com.example.test.ui.components.ChatItemComponent
import com.example.test.ui.components.SlideComponentBanner
import com.example.test.ui.components.SlideComponentProduct
import com.example.test.ui.components.SlideShoppingCategorys
import com.example.test.ui.dataTest.CategoryButtons
import com.example.test.ui.dataTest.banners
import com.example.test.ui.dataTest.products
import com.example.test.ui.dataTest.sampleChats
import com.example.test.ui.dataType.Chat
import com.example.test.ui.viewModels.ChatViewModel
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel
) {
    val user by authViewModel.user.collectAsState()
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var phoneNumber by remember { mutableStateOf("+6283822158268") }
    val context = LocalContext.current

    // Ambil daftar chat saat user login berubah
    LaunchedEffect(user?.uid) {
        user?.uid?.let { userId ->
            chatViewModel.fetchChats(userId) { chatList ->
                chats = chatList // Sekarang ini otomatis update di UI
            }
        }
    }

    // Filter daftar chat berdasarkan pencarian & filter yang dipilih
    val filteredChats by remember {
        derivedStateOf {
            chats.filter {
                (selectedFilter == "All" || (selectedFilter == "Group" && it.isGroup) || (selectedFilter == "Personal" && !it.isGroup)) &&
                        (searchQuery.isEmpty() || it.groupName?.contains(searchQuery, true) == true || it.participants.any { id -> id.contains(searchQuery, true) })
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { /* Aksi ketika tombol kontak ditekan */ }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_contacts_24),
                            contentDescription = "Kontak",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { /* Aksi ketika tombol lebih ditekan */ }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_more_vert_24),
                            contentDescription = "Lainnya",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (phoneNumber.isNotEmpty()) {
                        user?.let {
                            chatViewModel.getOrCreateChatByPhone(it.uid, phoneNumber) { chatId ->
                                if (chatId != null) {
                                    Toast.makeText(context, "Chat berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("chat_detail/$chatId")
                                } else {
                                    Toast.makeText(context, "Nomor tidak ditemukan!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 16.dp) // Sesuaikan posisi FAB
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Buat Pesan Baru")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            // Pencarian
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                placeholder = { Text("Cari chat atau pengguna...", fontSize = 14.sp) },
                shape = CircleShape,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp),
            )

            // Filter Chat
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(listOf("All", "Personal", "Group")) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        shape = CircleShape,
                    )
                }
            }

            // Daftar Chat
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredChats) { chat ->
                    val otherUserId = chat.participants.firstOrNull { it != user?.uid }
                    var otherUserProfileUrl by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(otherUserId) {
                        if (!chat.isGroup && otherUserId != null) {
                            chatViewModel.getUserProfile(otherUserId) { profileUrl ->
                                otherUserProfileUrl = profileUrl
                            }
                        }
                    }

                    ChatItemComponent(
                        chat = chat,
                        onClick = { navController.navigate("chat_detail/${chat.chatId}") },
                        otherUserProfileUrl = otherUserProfileUrl,
                        otherUserId = otherUserId,
                        userUid = user?.uid,
                        chatViewModel = chatViewModel
                    )
                }

                item {
                    Spacer(Modifier.height(120.dp))
                }
            }
        }
    }
}

