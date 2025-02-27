package com.example.test.ui.components
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import androidx.emoji2.emojipicker.EmojiPickerView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test.R
import com.example.test.ui.screens.User
import com.example.test.ui.viewModels.ChatViewModel
import com.google.gson.Gson
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ChatInputField(chatId: String, chatViewModel: ChatViewModel, user: User, participants: List<String>) {
    var text by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isEmojiPickerVisible by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Input Field dalam Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Tombol Emoji atau Keyboard
                    IconButton(onClick = {
                        if (isEmojiPickerVisible) {
                            // Jika emoji picker terbuka, sembunyikan dan buka keyboard
                            isEmojiPickerVisible = false
                            keyboardController?.show()
                        } else {
                            // Jika keyboard terbuka, sembunyikan dan tampilkan emoji picker
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            isEmojiPickerVisible = true
                        }
                    }) {
                        Icon(
                            painter = if (isEmojiPickerVisible) {
                                // Jika emoji picker terbuka, tampilkan ikon keyboard
                                painterResource(id = R.drawable.outline_keyboard_24)
                            } else {
                                // Jika keyboard terbuka, tampilkan ikon emoji
                                painterResource(id = R.drawable.outline_emoji_emotions_24)
                            },
                            contentDescription = "Emoji",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    TextField(
                        value = text,
                        onValueChange = { newValue ->
                            text = newValue
                            chatViewModel.updateTypingStatus(chatId, newValue.isNotEmpty(), user)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp, max = 150.dp)
                            .scrollable(scrollState, Orientation.Vertical)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    isEmojiPickerVisible = false
                                    if (keyboardController != null) {
                                        keyboardController.show()
                                    }
                                    chatViewModel.updateTypingStatus(chatId, true, user)
                                } else {
                                    chatViewModel.updateTypingStatus(chatId, false, user)
                                }
                            },

                        placeholder = { Text("Ketik pesan...") },
                        maxLines = Int.MAX_VALUE,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(fontSize = 16.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Tombol Kirim
            IconButton(
                onClick = {
                    val senderId = user.uid ?: return@IconButton // 🔥 Hindari NullPointerException

                    if (text.isNotBlank()) {
                        chatViewModel.sendMessage(
                            chatId = chatId,
                            senderId = senderId,
                            text = text,
                            mediaUrl = null, // 🔥 Gunakan null jika tidak ada media
                            mediaType = "text", // 🔥 Pastikan mediaType sesuai
                            participants = participants
                        )
                        text = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Kirim Pesan")
            }

        }

        // Emoji Picker View (Gunakan Jetpack Compose)
        if (isEmojiPickerVisible) {
            EmojiPicker(LocalContext.current) { emoji ->
                text += emoji
            }
        }
    }
}

@Composable
fun EmojiPicker(context: Context, onEmojiSelected: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    // 🔹 Load emoji dari JSON
    val emojiList = remember { loadEmojiListFromJson(context) }

    // 🔍 Filter berdasarkan deskripsi atau alias
    val filteredEmojis = remember(searchQuery) {
        if (searchQuery.isEmpty()) emojiList
        else emojiList.filter {
            it.description.contains(searchQuery, ignoreCase = true) ||
                    it.aliases.any { alias -> alias.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        // 🔍 TextField untuk pencarian emoji
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari emoji...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Hapus")
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🔎 Menampilkan hasil pencarian
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.LightGray)
        ) {
            items(filteredEmojis) { emojiData ->
                Text(
                    text = emojiData.emoji,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { onEmojiSelected(emojiData.emoji) }
                )
            }
        }
    }
}


data class EmojiData(
    val emoji: String,
    val description: String,
    val category: String,
    val aliases: List<String>,
    val tags: List<String>,
    val unicode_version: String,
    val ios_version: String
)


fun loadEmojiListFromJson(context: Context): List<EmojiData> {
    return try {
        val jsonString = context.assets.open("emoji.json").bufferedReader().use { it.readText() }
        val gson = Gson()
        val emojiArray = gson.fromJson(jsonString, Array<EmojiData>::class.java)
        emojiArray.toList()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}






