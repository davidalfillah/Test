package com.example.test.ui.viewModels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.ui.dataType.Chat
import com.example.test.ui.dataType.Message
import com.example.test.ui.screens.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // Menyimpan daftar chat pengguna
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    // Menyimpan daftar pesan dalam sebuah chat
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    // Menyimpan status mengetik
    private val _typingStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val typingStatus: StateFlow<Map<String, Boolean>> = _typingStatus

    // Menyimpan daftar chat yang sudah difilter
    private val _filteredChats = MutableStateFlow<List<Chat>>(emptyList())
    val filteredChats: StateFlow<List<Chat>> = _filteredChats

    fun getChatInfo(chatId: String): Flow<Chat?> = callbackFlow {
        val chatRef = db.collection("chats").document(chatId)
        val listener = chatRef.addSnapshotListener { snapshot, _ ->
            val chat = snapshot?.toObject(Chat::class.java)?.copy(chatId = snapshot.id)

            if (chat != null) {
                val participantsInfo = mutableMapOf<String, User?>()

                val jobs = chat.participants.map { userId ->
                    async {
                        val userSnapshot = db.collection("users").document(userId).get().await()
                        val user = userSnapshot.toObject(User::class.java)
                        participantsInfo[userId] = user // 🔥 Simpan dalam Map
                    }
                }

                GlobalScope.launch {
                    jobs.awaitAll()
                    trySend(chat.copy(participantsInfo = participantsInfo)) // ✅ Pakai Map<String, User?>
                }
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }




    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val query = db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, _ ->
            val messages = snapshot?.documents?.mapNotNull { it.toObject(Message::class.java) } ?: emptyList()
            trySend(messages)
        }
        awaitClose { listener.remove() }
    }

//    fun sendMessage(chatId: String, text: String, user: User) {
//        val newMessage = Message(
//            senderId = user.uid ?: "",
//            text = text,
//            timestamp = Timestamp.now()
//        )
//
//        db.collection("chats").document(chatId)
//            .collection("messages")
//            .add(newMessage)
//    }

    fun updateTypingStatus(chatId: String, isTyping: Boolean, user: User) {
        val chatRef = db.collection("chats").document(chatId)

        val updateData = if (isTyping) {
            mapOf("typingStatus.${user.uid}" to true)
        } else {
            mapOf("typingStatus.${user.uid}" to FieldValue.delete()) // Hapus key jika user berhenti mengetik
        }

        chatRef.update(updateData)
    }

    fun isTyping(chatId: String, currentUserId: String): Flow<Boolean> = callbackFlow {
        val chatRef = db.collection("chats").document(chatId)
        val listener = chatRef.addSnapshotListener { snapshot, _ ->
            val typingStatus = snapshot?.get("typingStatus") as? Map<String, Boolean> ?: emptyMap()
            val isOtherTyping = typingStatus.filterKeys { it != currentUserId }.values.any { it }
            trySend(isOtherTyping)
        }
        awaitClose { listener.remove() }
    }











    fun fetchChats(userId: String, onSuccess: (List<Chat>) -> Unit) {
        db.collection("chats")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("fetchChats", "Error fetching chats: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val chatList = it.documents.mapNotNull { doc ->
                        doc.toObject(Chat::class.java)?.copy(chatId = doc.id)
                    }

                    if (chatList.isEmpty()) {
                        onSuccess(emptyList()) // Tidak ada chat, langsung return
                        return@let
                    }

                    val updatedChats = mutableListOf<Chat>()
                    var counter = 0

                    chatList.forEach { chat ->
                        val participantsData = mutableMapOf<String, User?>()

                        chat.participants.forEach { participantId ->
                            db.collection("users").document(participantId)
                                .get()
                                .addOnSuccessListener { userSnapshot ->
                                    val user = userSnapshot.toObject(User::class.java)?.copy(uid = participantId)
                                    participantsData[participantId] = user

                                    // Jika sudah mengambil semua data user, update chat list
                                    if (participantsData.size == chat.participants.size) {
                                        updatedChats.add(chat.copy(participantsInfo = participantsData))
                                        counter++

                                        if (counter == chatList.size) {
                                            onSuccess(updatedChats) // Kembalikan hasil akhir ke UI
                                        }
                                    }
                                }
                        }
                    }
                }
            }
    }




    // Pencarian dan filter dalam data yang sudah ada (tanpa query ulang ke Firestore)
    fun searchChats(query: String, filter: String) {
        val filtered = _chats.value.filter {
            (filter == "All" || (filter == "Group" && it.isGroup) || (filter == "Personal" && !it.isGroup)) &&
                    (query.isEmpty() || it.groupName?.contains(query, true) == true || it.lastMessage.contains(query, true))
        }
        _filteredChats.value = filtered
    }


    // Mengambil pesan dalam chat
    fun fetchMessages(chatId: String) {
        viewModelScope.launch {
            db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.let {
                        _messages.value = it.toObjects()
                    }
                }
        }
    }


    fun getUserProfile(userId: String, onResult: (String?) -> Unit) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val profileUrl = document.getString("profileImageUrl")
                onResult(profileUrl)
            }
    }

    // Mengirim pesan baru
    fun sendMessage(chatId: String, senderId: String, text: String?, mediaUrl: String?, mediaType: String) {
        val messageId = db.collection("chats").document(chatId)
            .collection("messages").document().id

        val message = Message(
            messageId = messageId,
            senderId = senderId,
            text = text,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            timestamp = Timestamp.now(),
            status = "sent"
        )

        db.collection("chats").document(chatId)
            .collection("messages").document(messageId).set(message)

        // Update last message di chat
        db.collection("chats").document(chatId).update(
            "lastMessage", text ?: "Media",
            "lastMessageType", mediaType,
            "lastMessageTimestamp", Timestamp.now(),
            "lastSenderId", senderId
        )
    }

    fun getUnreadCount(chatId: String, userId: String, onResult: (Int) -> Unit) {
        db.collection("chats").document(chatId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val unreadData = snapshot.get("unreadCount") as? Map<String, Long>
                    val unreadCount = unreadData?.get(userId)?.toInt() ?: 0
                    onResult(unreadCount)
                }
            }
    }

    fun markChatAsRead(chatId: String, userId: String) {
        db.collection("chats").document(chatId)
            .update("unreadCount.$userId", 0)
    }

    // Menarik pesan (hapus untuk semua orang)
    fun deleteMessageForEveryone(chatId: String, messageId: String) {
        db.collection("chats").document(chatId)
            .collection("messages").document(messageId)
            .update("deletedForEveryone", true)
    }

    // Menyimpan status mengetik
    fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        db.collection("typingStatus").document(chatId)
            .update(userId, isTyping)
    }

    // Update status pesan (sent -> delivered -> read)
    fun updateMessageStatus(chatId: String, messageId: String, status: String) {
        db.collection("chats").document(chatId)
            .collection("messages").document(messageId)
            .update("status", status)
    }

    // Menandai semua pesan dalam chat sebagai "read" saat pengguna membuka chat
    fun markMessagesAsRead(chatId: String, userId: String) {
        viewModelScope.launch {
            val messagesRef = db.collection("chats").document(chatId)
                .collection("messages")
                .whereNotEqualTo("senderId", userId) // Hanya untuk pesan yang dikirim orang lain

            messagesRef.get().addOnSuccessListener { documents ->
                for (doc in documents) {
                    doc.reference.update("status", "read")
                }
            }
        }
    }

    // Set user online saat membuka aplikasi
    fun setUserOnline(userId: String, isOnline: Boolean) {
        val updateData = if (isOnline) {
            mapOf("isOnline" to true)
        } else {
            mapOf("isOnline" to false, "lastSeen" to System.currentTimeMillis())
        }

        db.collection("users").document(userId).update(updateData)
    }

    // Mendengarkan perubahan status online dari teman chat
    fun listenUserStatus(userId: String, onStatusChanged: (Boolean, Long) -> Unit) {
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val isOnline = snapshot.getBoolean("isOnline") ?: false
                    val lastSeen = snapshot.getLong("lastSeen") ?: 0L
                    onStatusChanged(isOnline, lastSeen)
                }
            }
    }

    // Hapus pesan hanya untuk diri sendiri
    fun deleteMessageForSelf(chatId: String, messageId: String, userId: String) {
        db.collection("chats").document(chatId)
            .collection("messages").document(messageId)
            .update("deletedForUser.$userId", true)
    }

    fun fetchMessagesWithPagination(chatId: String, lastMessage: Message?, limit: Long = 20) {
        var query = db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .limit(limit)

        lastMessage?.let {
            query = query.startAfter(it.timestamp)
        }

        query.addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                _messages.value = _messages.value + it.toObjects()
            }
        }
    }


    fun getOrCreateChatByPhone(userId: String, phoneNumber: String, onResult: (String?) -> Unit) {
        getUserIdByPhone(phoneNumber) { otherUserId ->
            if (otherUserId != null) {
                getOrCreateChat(userId, otherUserId) { chatId ->
                    onResult(chatId) // Kembalikan ID chat yang ditemukan atau dibuat
                }
            } else {
                onResult(null) // User dengan nomor HP tidak ditemukan
            }
        }
    }


    fun getOrCreateChat(userId: String, otherUserId: String, onResult: (String) -> Unit) {
        db.collection("chats")
            .whereEqualTo("isGroup", false)
            .whereArrayContains("participants", userId)
            .get()
            .addOnSuccessListener { documents ->
                val existingChat = documents.documents.find { doc ->
                    val participants = doc.get("participants") as List<String>
                    otherUserId in participants
                }

                if (existingChat != null) {
                    onResult(existingChat.id) // Chat sudah ada
                } else {
                    // Buat chat baru
                    val newChat = hashMapOf(
                        "participants" to listOf(userId, otherUserId),
                        "isGroup" to false,
                        "lastMessage" to "",
                        "lastMessageTimestamp" to Timestamp.now()
                    )

                    db.collection("chats").add(newChat).addOnSuccessListener { docRef ->
                        onResult(docRef.id) // Kembalikan ID chat yang baru
                    }
                }
            }
    }


    private fun getUserIdByPhone(phoneNumber: String, onResult: (String?) -> Unit) {
        db.collection("users")
            .whereEqualTo("phone", phoneNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userId = documents.documents.first().id
                    onResult(userId)
                } else {
                    onResult(null) // User tidak ditemukan
                }
            }
    }

    fun sendMessageByPhone(phoneNumber: String, senderId: String, message: String) {
        getUserIdByPhone(phoneNumber) { otherUserId ->
            if (otherUserId != null) {
                getOrCreateChat(senderId, otherUserId) { chatId ->
                    sendMessage(
                        chatId, senderId, message,
                        mediaUrl = TODO(),
                        mediaType = TODO()
                    )
                }
            } else {
                Log.e("Chat", "Nomor HP tidak ditemukan!")
            }
        }
    }




    // Mendengarkan status mengetik
    fun listenTypingStatus(chatId: String) {
        db.collection("typingStatus").document(chatId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    _typingStatus.value = it.data as Map<String, Boolean>? ?: emptyMap()
                }
            }
    }
}


