package com.example.test.ui.dataType

import androidx.annotation.Keep
import com.example.test.ui.screens.User
import com.google.firebase.Timestamp

data class Chat(
    val chatId: String = "",
    val participants: List<String> = listOf(), // Daftar user dalam chat
    val participantsInfo: Map<String, User?> = mapOf(), // Informasi tambahan user dalam chat: List<String>
    val groupName: String? = null, // Nama grup (jika grup chat)
    val groupImageUrl: String? = null, // Foto grup (jika grup chat)
    val lastMessage: String = "", // Isi pesan terakhir
    val lastMessageType: String = "text", // Jenis pesan terakhir (text, image, dll.)
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val lastSenderId: String = "", // ID pengirim pesan terakhir,
    val isGroup: Boolean = false, // Apakah chat ini grup?
    val unreadCount: Map<String, Int> = mapOf()
)

data class Message(
    val messageId: String = "",
    val senderId: String = "", // Pengirim pesan
    val receiverId: String? = null, // Penerima (kosong jika grup)
    val text: String? = null, // Isi pesan (jika teks)
    val mediaUrl: String? = null, // URL media (jika ada)
    val mediaType: String = "text", // Jenis media (text, image, video, file, audio, sticker)
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "sent", // Status pesan (sent, delivered, read)
    val replyTo: String? = null, // ID pesan yang dibalas
    val reactions: Map<String, String> = mapOf(), // Reaksi emoji per user (userId -> emoji)
    val forwarded: Boolean = false, // Apakah pesan diteruskan?
    val edited: Boolean = false, // Apakah pesan sudah diedit?
    val deletedForEveryone: Boolean = false // Apakah pesan ditarik?
)

data class MessageStatus(
    val messageId: String = "",
    val userId: String = "", // User yang menerima/baca pesan
    val status: String = "sent", // Status pesan: sent, delivered, read
    val timestamp: Timestamp = Timestamp.now(),
)

data class TypingStatus(
    val chatId: String = "",
    val userId: String = "",
    val isTyping: Boolean = false // True jika sedang mengetik
)


