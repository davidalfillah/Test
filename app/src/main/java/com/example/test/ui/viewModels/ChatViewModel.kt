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
import kotlinx.coroutines.DelicateCoroutinesApi
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
                launch {
                    val participantsInfo = mutableMapOf<String, User?>()

                    chat.participants.forEach { userId ->
                        val userSnapshot = db.collection("users").document(userId).get().await()
                        val user = userSnapshot.toObject(User::class.java)
                        participantsInfo[userId] = user // 🔥 Simpan dalam Map
                    }

                    trySend(chat.copy(participantsInfo = participantsInfo)) // ✅ Pakai Map<String, User?>
                }
            } else {
                trySend(null)
            }
        }

        awaitClose { listener.remove() }
    }





    fun getMessages(chatId: String, userId: String): Flow<List<Message>> = callbackFlow {
        val chatRef = db.collection("chats").document(chatId)
        val query = chatRef.collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, _ ->
            val messages = snapshot?.documents?.mapNotNull { it.toObject(Message::class.java) } ?: emptyList()
            trySend(messages)

            // 🔥 Jalankan coroutine di dalam `callbackFlow`
            launch {
                val chatSnapshot = chatRef.get().await()
                val chat = chatSnapshot.toObject(Chat::class.java)

                chat?.let {
                    if (it.lastSenderId != userId) {
                        resetUnreadCount(chatId, userId)
                        markMessagesAsRead(chatId, userId)
                    }
                }
            }
        }

        awaitClose { listener.remove() }
    }


    suspend fun markMessagesAsRead(chatId: String, userId: String) {
        val chatRef = db.collection("chats").document(chatId)
        val messagesRef = chatRef.collection("messages")

        try {
            // 🔥 Ambil semua pesan yang masih memiliki unreadBy = userId sebelum transaksi
            val snapshot = messagesRef.whereArrayContains("unreadBy", userId).get().await()

            db.runTransaction { transaction ->
                val chatSnapshot = transaction.get(chatRef)
                val lastUnreadBy = chatSnapshot.get("lastUnreadBy") as? List<String> ?: listOf()

                val updatedUnreadBy = lastUnreadBy.filter { it != userId }

                // 🔥 Update setiap pesan untuk menghapus userId dari unreadBy
                for (doc in snapshot.documents) {
                    transaction.update(doc.reference, "unreadBy", FieldValue.arrayRemove(userId))
                }

                // 🔥 Perbarui lastUnreadBy di chat untuk tracking pesan belum dibaca
                transaction.update(chatRef, "lastUnreadBy", updatedUnreadBy)
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error marking messages as read: ${e.message}")
        }
    }






    fun deleteMessageForEveryone(chatId: String, messageId: String) {
        db.collection("chats").document(chatId)
            .collection("messages").document(messageId)
            .update(
                "text", "Pesan telah dihapus",
                "mediaUrl", null,
                "deletedForEveryone", true
            )
    }


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

    fun sendMessage(
        chatId: String,
        senderId: String,
        text: String?,
        mediaUrl: String?,
        mediaType: String,
        participants: List<String>
    ) {
        require(participants.isNotEmpty()) { "Participants list cannot be empty" } // 🔥 Pastikan peserta ada

        val messageId = db.collection("chats").document(chatId)
            .collection("messages").document().id

        Log.d("sendMessage", "Participants: $participants") // 🔥 Debugging

        val unreadBy = participants.filter { it != senderId } // 🚀 Semua user kecuali pengirim

        val message = Message(
            messageId = messageId,
            senderId = senderId,
            text = text?.takeIf { it.isNotBlank() }, // 🔥 Hindari pesan kosong
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            timestamp = Timestamp.now(),
            unreadBy = unreadBy
        )

        val chatRef = db.collection("chats").document(chatId)

        // 🚀 Simpan pesan ke Firestore
        chatRef.collection("messages").document(messageId).set(message)
            .addOnSuccessListener {
                Log.d("sendMessage", "Message sent successfully!")

                // ✅ Update informasi chat terakhir
                chatRef.update(
                    "lastMessage", text ?: "Media",
                    "lastMessageType", mediaType,
                    "lastMessageTimestamp", Timestamp.now(),
                    "lastSenderId", senderId,
                    "lastUnreadBy", unreadBy // ✅ Simpan user yang belum membaca
                )

                // 🚀 Tambahkan unread count untuk semua user kecuali pengirim
                incrementUnreadCount(chatId, senderId)
            }
            .addOnFailureListener { e ->
                Log.e("sendMessage", "Failed to send message: ${e.message}")
            }
    }


    private fun incrementUnreadCount(chatId: String, senderId: String) {
        val chatRef = FirebaseFirestore.getInstance().collection("chats").document(chatId)

        chatRef.get().addOnSuccessListener { document ->
            val chat = document.toObject(Chat::class.java)
            chat?.let {
                val updates = mutableMapOf<String, Any>()

                // Tambah unreadCount untuk semua user KECUALI pengirim
                it.participants.forEach { userId ->
                    if (userId != senderId) {
                        updates["unreadCount.$userId"] = FieldValue.increment(1)
                    }
                }

                // Update Firestore secara atomik
                chatRef.update(updates)
            }
        }
    }


    private fun resetUnreadCount(chatId: String, userId: String) {
        val chatRef = db.collection("chats").document(chatId)

        chatRef.get().addOnSuccessListener { document ->
            val chat = document.toObject(Chat::class.java)
            chat?.let {
                // 🔥 Hanya reset jika user BUKAN pengirim pesan terakhir
                if (it.lastSenderId != userId) {
                    chatRef.update("unreadCount.$userId", 0)
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


    private fun getOrCreateChat(userId: String, otherUserId: String, onResult: (String) -> Unit) {
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
                        mediaType = TODO(),
                        participants = listOf(senderId, otherUserId)
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


