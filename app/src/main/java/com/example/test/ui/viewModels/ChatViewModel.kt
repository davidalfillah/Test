package com.example.test.ui.viewModels

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.example.test.AuthRepository
import com.example.test.AuthViewModel
import com.example.test.ui.dataType.ChatData
import com.example.test.ui.dataType.ChatUserData
import com.example.test.ui.dataType.Message
import com.example.test.ui.screens.User
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun listenToMessages(chatId: String, onResult: (List<Message>) -> Unit) {
        Firebase.firestore.collection("chats").document(chatId).collection("messages")
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error fetching messages", error)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    Log.d("ChatViewModel", "No messages found")
                    onResult(emptyList())  // Kembalikan list kosong jika tidak ada pesan
                    return@addSnapshotListener
                }

                val messageList = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                onResult(messageList)  // Kirim hasilnya melalui callback
            }
    }



    /**
     * 🔹 Mengirim pesan ke Firestore
     */
    fun sendMessage(chatId: String, content: String, senderId: String) {
        val messageRef = Firebase.firestore.collection("chats").document(chatId).collection("messages").document()
        val timestamp = Timestamp.now()

        val message = Message(
            msgId = messageRef.id,
            senderId = senderId,
            content = content,
            time = timestamp
        )

        messageRef.set(message)
            .addOnSuccessListener {
                Log.d("ChatViewModel", "Message sent successfully")

                // Setelah pesan dikirim, perbarui "last" di chats/{chatId}
                updateLastMessage(chatId, message)
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error sending message", e)
            }
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
                    val unreadCount = if (isUser1) chatData.user1?.unread ?: 0 else chatData.user2?.unread ?: 0

                    // Jika unread masih ada, reset
                    if (unreadCount > 0) {
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

