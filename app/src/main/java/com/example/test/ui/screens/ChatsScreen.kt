package com.example.test.ui.screens

import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.test.ui.dataType.ChatData
import com.example.test.ui.dataType.ChatUserData
import com.example.test.ui.viewModels.ChatViewModel
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(navController: NavHostController, paddingValues: PaddingValues, chatViewModel: ChatViewModel, authViewModel: AuthViewModel) {
    val user by authViewModel.user.collectAsState()
    val currentUserPhone = user?.phone ?: ""
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }

    val filters = listOf("Semua", "Belum Dibaca", "Hari Ini", "Minggu Ini")


    val chats = remember { mutableStateOf<List<ChatData>>(emptyList()) }

    LaunchedEffect(currentUserPhone) {
        chatViewModel.listenToChats(currentUserPhone) { newChats ->
            chats.value = newChats
        }
    }



//     Filter chats berdasarkan searchQuery dan selectedFilter
    val filteredChats = chats.value.filter { chat ->
        (chat.user2?.name?.contains(searchQuery, ignoreCase = true) == true ||
                chat.last?.msgId?.contains(searchQuery, ignoreCase = true) == true) &&
                when (selectedFilter) {
                    "Belum Dibaca" -> chat.user2?.unread!! > 0
                    "Hari Ini" -> chat.last?.time.toString().contains("AM") || chat.last?.time.toString().contains("PM")  // Misalnya, bisa diganti dengan waktu sebenarnya
                    "Minggu Ini" -> chat.last?.time.toString() in listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    else -> true
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
                    IconButton(onClick = { /* Aksi ketika tombol pencarian ditekan */ }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_contacts_24),
                            contentDescription = "Contacs",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { /* Aksi ketika tombol pencarian ditekan */ }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_more_vert_24),
                            contentDescription = "Contacs",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    user?.let { chatViewModel.addChat(it, "081234567890") }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .offset(y = (-80).dp) // Mengangkat FAB ke atas agar tidak tertutup
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
                    top = innerPadding.calculateTopPadding(),  // Jaga jarak dari AppBar
                    bottom = paddingValues.calculateBottomPadding() // Hindari tumpang tindih dengan BottomNav
                )
        ) {

            // Chat List (Scrollable Vertically)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)

            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(48.dp), // Ukuran lebih kecil
                        placeholder = { Text("Search...", fontSize = 14.sp) },
                        shape = CircleShape, // Bentuk bulat sempurna
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp), // Ukuran teks lebih kecil
                    )

                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp) // Padding hanya di awal dan akhir
                    ) {
                        items(filters) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                shape = CircleShape,
                            )
                        }
                    }
                }
                items(filteredChats) { chat ->
                    val chatUser: ChatUserData? = if (user?.uid != null && chat.user1?.userId != user!!.uid) {
                        chat.user1
                    } else {
                        chat.user2
                    }
                    chatUser?.let { currentUser ->
                        chat.last?.let { lastMessage ->
                            val isUserMessage = lastMessage.senderId == user?.uid
                            ChatItemComponent(currentUser, lastMessage, onClick = {navController.navigate("chat_detail/${chat.chatId}")}, isUserMessage)
                        }
                    }
                }


                item {
                    Spacer(Modifier.height(120.dp))
                }
            }
        }
    }
}
