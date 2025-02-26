package com.example.test.ui.viewModels

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.AuthRepository
import com.example.test.AuthViewModel
import com.example.test.ui.dataType.ChatData
import com.example.test.ui.dataType.ChatUserData
import com.example.test.ui.dataType.Message
import com.example.test.ui.screens.User
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

    fun getChatUser(chatId: String, onResult: (ChatUserData) -> Unit) {
        Firebase.firestore.collection("chats").document(chatId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val chatData = document.toObject(ChatData::class.java)
                    val currentUserId = Firebase.auth.currentUser?.uid

                    val userData = if (chatData?.user1?.userId == currentUserId) {
                        chatData?.user2
                    } else {
                        chatData?.user1
                    }

                    if (userData != null) {
                        onResult(userData)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ChatViewModel", "Error fetching chat user", exception)
            }
    }

    fun loadInitialMessages(chatId: String, onResult: (List<Message>, DocumentSnapshot?) -> Unit) {
        Firebase.firestore.collection("chats").document(chatId).collection("messages")
            .orderBy("time", Query.Direction.DESCENDING)
            .limit(20) // 🔹 Ambil 20 pesan terbaru
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onResult(emptyList(), null)
                    return@addOnSuccessListener
                }

                val messages = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                val lastVisibleMessage = snapshot.documents.last() // 🔹 Simpan last message untuk pagination

                onResult(messages.reversed(), lastVisibleMessage) // 🔹 Balik urutan agar pesan terbaru di bawah
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error fetching messages", e)
                onResult(emptyList(), null)
            }
    }

    fun listenForNewMessages(chatId: String, currentUserId: String, onNewMessage: (Message) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                for (doc in snapshots.documentChanges) {
                    if (doc.type == DocumentChange.Type.ADDED) {
                        val newMessage = doc.document.toObject(Message::class.java)

                        // Hanya reset unread jika pesan baru ditujukan ke pengguna yang sedang membuka chat
                        if (newMessage.senderId != currentUserId) {
                            viewModelScope.launch {
                                resetUnreadMessages(chatId, currentUserId)
                                markAsRead(chatId, newMessage.msgId)
                            }
                        }

                        onNewMessage(newMessage)
                    }
                }
            }
    }


    fun loadMoreMessages(chatId: String, lastMessage: DocumentSnapshot?, onResult: (List<Message>, DocumentSnapshot?) -> Unit) {
        if (lastMessage == null) return // 🔹 Jika tidak ada pesan sebelumnya, tidak perlu load lagi

        Firebase.firestore.collection("chats").document(chatId).collection("messages")
            .orderBy("time", Query.Direction.DESCENDING)
            .startAfter(lastMessage) // 🔹 Mulai dari pesan terakhir yang sudah diambil
            .limit(20)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onResult(emptyList(), null)
                    return@addOnSuccessListener
                }

                val newMessages = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                val newLastVisibleMessage = snapshot.documents.last()

                onResult(newMessages.reversed(), newLastVisibleMessage) // 🔹 Balik urutan agar pesan terbaru tetap di bawah
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error fetching more messages", e)
                onResult(emptyList(), lastMessage)
            }
    }




    /**
     * 🔹 Mengirim pesan ke Firestore
     */
    fun sendMessage(chatId: String, content: String, senderId: String) {
        val messageRef = Firebase.firestore.collection("chats").document(chatId)
            .collection("messages").document()
        val timestamp = Timestamp.now()

        val newMessage = Message(
            msgId = messageRef.id,
            senderId = senderId,
            content = content,
            time = timestamp,
            status = "pending"
        )

        messageRef.set(newMessage)
            .addOnSuccessListener {
                Log.d("ChatViewModel", "Message sent successfully")

                // Update status menjadi "sent"
                messageRef.update("status", "sent")
                    .addOnFailureListener { e ->
                        Log.e("ChatViewModel", "Failed to update message status to sent", e)
                    }

                updateLastMessage(chatId, newMessage)
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error sending message", e)

                // Jika gagal, ubah status menjadi "failed"
                messageRef.update("status", "failed")
                    .addOnFailureListener { updateError ->
                        Log.e("ChatViewModel", "Failed to update message status to failed", updateError)
                    }
            }
    }

    private fun markAsRead(chatId: String, messageId: String) {
        val db = Firebase.firestore
        val chatRef = db.collection("chats").document(chatId)
        val messageRef = chatRef.collection("messages").document(messageId)

        db.runTransaction { transaction ->
            val messageSnapshot = transaction.get(messageRef)
            val lastSnapshot = transaction.get(chatRef)

            if (messageSnapshot.exists()) {
                // Update status di messages
                transaction.update(messageRef, "status", "read")

                // Update status di chats.last.status langsung
                val lastMsgId = lastSnapshot.getString("last.msgId")
                Log.d("ChatViewModel", "Last message ID: $lastMsgId, Current message ID: $messageId")

                if (lastMsgId == messageId) {
                    Log.d("ChatViewModel", "Updating last.status to read")
                    transaction.update(chatRef, "last.status", "read")
                }
            }
        }.addOnSuccessListener {
            Log.d("ChatViewModel", "Message and last status updated successfully")
        }.addOnFailureListener { e ->
            Log.e("ChatViewModel", "Failed to update message and last status", e)
        }
    }





    fun recallMessage(chatId: String, messageId: String) {
        Firebase.firestore.collection("chats").document(chatId)
            .collection("messages").document(messageId)
            .update("content", "Pesan telah ditarik", "status", "recalled")
    }



    fun markAsDelivered(chatId: String, messageId: String) {
        Firebase.firestore.collection("chats").document(chatId)
            .collection("messages").document(messageId)
            .update("status", "delivered")
    }



    private fun updateLastMessage(chatId: String, lastMessage: Message) {
        val chatRef = Firebase.firestore.collection("chats").document(chatId)

        Firebase.firestore.runTransaction { transaction ->
            val chatSnapshot = transaction.get(chatRef)
            val chatData = chatSnapshot.toObject(ChatData::class.java)

            if (chatData != null) {
                val isSenderUser1 = chatData.user1?.userId == lastMessage.senderId
                val updatedChat = chatData.copy(
                    last = lastMessage,
                    user1 = if (!isSenderUser1) chatData.user1 else chatData.user1?.copy(unread = chatData.user1.unread + 1),
                    user2 = if (isSenderUser1) chatData.user2 else chatData.user2?.copy(unread = chatData.user2.unread + 1)
                )
                transaction.set(chatRef, updatedChat)
            }
        }.addOnSuccessListener {
            Log.d("ChatViewModel", "Last message and unread count updated successfully")
        }.addOnFailureListener { e ->
            Log.e("ChatViewModel", "Error updating last message and unread count", e)
        }
    }

    fun listenToUnreadUpdates(chatId: String, currentUserId: String) {
        val chatRef = Firebase.firestore.collection("chats").document(chatId)

        chatRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ChatViewModel", "Error listening to unread updates", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val chatData = snapshot.toObject(ChatData::class.java)
                if (chatData != null) {
                    val isUser1 = chatData.user1?.userId == currentUserId
                    val unreadCount = if (isUser1) chatData.user2?.unread ?: 0 else chatData.user1?.unread ?: 0

                    // 🔹 Reset unread hanya jika masih ada dan user benar-benar di chat screen
                    if (unreadCount > 0) {
                        Log.d("ChatViewModel", "Resetting unread count for chatId: $chatId, userId: $currentUserId")
                        resetUnreadMessages(chatId, currentUserId)
                    }
                }
            }
        }
    }



    fun resetUnreadMessages(chatId: String, currentUserId: String) {
        val chatRef = Firebase.firestore.collection("chats").document(chatId)

        Firebase.firestore.runTransaction { transaction ->
            val chatSnapshot = transaction.get(chatRef)
            val chatData = chatSnapshot.toObject(ChatData::class.java)

            if (chatData != null) {
                val isUser1 = chatData.user1?.userId == currentUserId
                val updatedChat = chatData.copy(
                    user1 = if (!isUser1) chatData.user1?.copy(unread = 0) else chatData.user1,
                    user2 = if (isUser1) chatData.user2?.copy(unread = 0) else chatData.user2
                )
                transaction.set(chatRef, updatedChat)
            }
        }.addOnSuccessListener {
            Log.d("ChatViewModel", "Unread messages reset successfully")
        }.addOnFailureListener { e ->
            Log.e("ChatViewModel", "Error resetting unread messages", e)
        }
    }


    /**
     * 🔹 Set unread menjadi 0 jika userId adalah pemilik akun yang membuka chat
     */
    private fun resetUnreadIfNeeded(user: ChatUserData, userId: String): ChatUserData {
        return if (user.userId == userId) {
            user.copy(unread = 0) // Reset unread jika pengguna membuka chat
        } else {
            user // Jika bukan user yang membuka, tetap sama
        }
    }


    /**
     * 🔹 Perbarui jumlah pesan yang belum dibaca pada penerima
     */
    private fun updateUnread(user: ChatUserData, lastMessage: Message): ChatUserData {
        return if (user.userId != lastMessage.senderId) {
            user.copy(unread = user.unread + 1) // Tambahkan unread jika bukan pengirim
        } else {
            user // Jika pengirim, jangan ubah unread
        }
    }

    // ✅ Perbaikan Query: Ambil chat di mana user adalah user1 atau user2
    fun listenToChats(currentUserPhone: String, onResult: (List<ChatData>) -> Unit) {
        Log.d("ChatViewModel", "Listening for chats with phone: $currentUserPhone")

        Firebase.firestore.collection("chats")
            .where(
                Filter.or(
                    Filter.equalTo("user1.phone", currentUserPhone),
                    Filter.equalTo("user2.phone", currentUserPhone)
                )
            )
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error fetching chats", error)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    Log.d("ChatViewModel", "No chats found")
                    onResult(emptyList()) // Mengembalikan list kosong
                    return@addSnapshotListener
                }

                Log.d("ChatViewModel", "Chats fetched successfully")

                val chatList = snapshot.documents.mapNotNull {
                    Log.d("ChatViewModel", "Chat data: ${it.data}")
                    it.toObject(ChatData::class.java)
                }
                onResult(chatList) // Mengembalikan list chat ke callback
            }
    }




    // ✅ Perbaikan: Tambahkan validasi & error handling
    fun addChat(currentUser: User, phone: String) {

        Firebase.firestore.collection("chats").where(
            Filter.or(
                Filter.and(
                    Filter.equalTo("user1.phone", currentUser.phone),
                    Filter.equalTo("user2.phone", phone)
                ),
                Filter.and(
                    Filter.equalTo("user1.phone", phone),
                    Filter.equalTo("user2.phone", currentUser.phone)
                )
            )
        ).get()
            .addOnSuccessListener { chatSnapshot ->
                if (!chatSnapshot.isEmpty) {
                    println("Chat already exists")
                    return@addOnSuccessListener
                }

                Firebase.firestore.collection("users").whereEqualTo("phone", phone).get()
                    .addOnSuccessListener { userSnapshot ->
                        if (userSnapshot.isEmpty) {
                            println("User not found")
                            return@addOnSuccessListener
                        }

                        val chatPartner = userSnapshot.toObjects(User::class.java).firstOrNull()
                        if (chatPartner == null || chatPartner.uid.isNullOrEmpty()) {
                            println("Chat partner data is invalid")
                            return@addOnSuccessListener
                        }

                        val chatId = Firebase.firestore.collection("chats").document().id
                        val chat = ChatData(
                            chatId = chatId,
                            last = Message(
                                senderId = "",
                                content = "",
                                time = null,
                            ),
                            user1 = ChatUserData(
                                userId = currentUser.uid,
                                bio = "",
                                name = currentUser.name,
                                typing = false,
                                imageUrl = currentUser.profilePicUrl,
                                phone = currentUser.phone,
                                status = "",
                                unread = 0,
                            ),
                            user2 = ChatUserData(
                                userId = chatPartner.uid,
                                bio = "",
                                name = chatPartner.name ?: "",
                                typing = false,
                                imageUrl = chatPartner.profilePicUrl ?: "",
                                phone = chatPartner.phone ?: "",
                                status = "",
                                unread = 0,
                            )
                        )

                        Firebase.firestore.collection("chats").document(chatId).set(chat)
                            .addOnSuccessListener {
                                println("Chat created successfully")
                            }
                            .addOnFailureListener { exception ->
                                println("Error creating chat: ${exception.message}")
                            }
                    }
                    .addOnFailureListener { exception ->
                        println("Error fetching user: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                println("Error checking existing chat: ${exception.message}")
            }
    }
}

