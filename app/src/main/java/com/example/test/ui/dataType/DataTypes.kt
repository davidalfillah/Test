package com.example.test.ui.dataType

import androidx.annotation.Keep
import com.google.firebase.Timestamp

@Keep
data class ChatUserData(
    val userId: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val phone: String = "",
    val bio: String = "",
    val status: String = "",
    val unread: Int = 0,
    val typing: Boolean = false
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "imageUrl" to imageUrl,
            "phone" to phone,
            "bio" to bio,
            "status" to status,
            "unread" to unread,
            "typing" to typing
        )
    }
}

@Keep
data class ChatData(
    val chatId: String = "",
    val last: Message? = null,
    val user1: ChatUserData? = null,
    val user2: ChatUserData? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "chatId" to chatId,
            "last" to last?.toMap(),
            "user1" to user1?.toMap(),
            "user2" to user2?.toMap()
        )
    }
}

@Keep
data class Reaction(
    val imageUrl: String = "",
    val name: String = "",
    val userId: String = "",
    val reaction: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "imageUrl" to imageUrl,
            "name" to name,
            "userId" to userId,
            "reaction" to reaction
        )
    }
}

@Keep
data class Message(
    val msgId: String = "",
    val senderId: String = "",
    val replyMessage: Message? = null,
    val reactions: List<Reaction> = emptyList(), // Mengubah dari List<String> ke List<Reaction> agar lebih fleksibel
    val imageUrl: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val fileSize: String = "",
    val videoUrl: String = "",
    val progress: String = "",
    val status: String = "",
    val content: String = "",
    val time: Timestamp? = null,
    val forwarded: Boolean = false
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "msgId" to msgId,
            "senderId" to senderId,
            "replyMessage" to replyMessage?.toMap(),
            "reactions" to reactions.map { it.toMap() },
            "imageUrl" to imageUrl,
            "fileUrl" to fileUrl,
            "fileName" to fileName,
            "fileSize" to fileSize,
            "videoUrl" to videoUrl,
            "progress" to progress,
            "content" to content,
            "time" to time,
            "forwarded" to forwarded
        )
    }
}


