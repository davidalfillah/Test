package com.example.test.ui.dataType

import com.example.test.ui.screens.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

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
    val unreadCount: Map<String, Int> = mapOf(),
    val lastUnreadBy: List<String> = listOf() // ✅ Tambahkan ini
)

data class Message(
    val messageId: String = "",
    val senderId: String = "", // Pengirim pesan
    val receiverId: String? = null, // Penerima (kosong jika grup)
    val text: String? = null, // Isi pesan (jika teks)
    val mediaUrl: String? = null, // URL media (jika ada)
    val mediaType: String = "text", // Jenis media (text, image, video, file, audio, sticker)
    val timestamp: Timestamp = Timestamp.now(),
    val unreadBy: List<String> = listOf(), // ✅ Menyimpan user yang BELUM membaca
    val replyTo: String? = null, // ID pesan yang dibalas
    val reactions: Map<String, String> = mapOf(), // Reaksi emoji per user (userId -> emoji)
    val forwarded: Boolean = false, // Apakah pesan diteruskan?
    val edited: Boolean = false, // Apakah pesan sudah diedit?
    val deletedForEveryone: Boolean = false, // Apakah pesan ditarik?
)

data class Address(
    val street: String = "",
    val village: String = "",
    val subDistrict: String = "", // Kecamatan
    val city: String = "", // Kabupaten/Kota
    val province: String = "",
    val postalCode: String = ""
)

data class Member(
    val id: String? = "",
    val userId: String = "",  // User ID dari Firebase Authentication
    val memberId: String = "",
    val fullName: String = "",
    val nik: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val religion: String = "",
    val education: String = "",
    val joinDateDay: Timestamp = Timestamp.now(),
    val phone: String = "",
    val job: String = "",
    val address: Address = Address(),
    val branchId: String = "",
    val branchLevel: BranchLevel = BranchLevel.PAC,
    val jobTitle: String = "Anggota",
    val status: String = "active",
    val createdAt: Timestamp = Timestamp.now(),
    val umkmIds: List<String> = emptyList(),
)

data class Umkm(
    val umkmId: String = "",
    val ownerId: String = "",
    val name: String = "",
    val businessType: String = "",
    val description: String = "",
    val address: Address = Address(),
    val contact: String = "",
    val registrationDate: Long = System.currentTimeMillis(),
    val status: String = "active"
)



enum class BranchLevel {
    PAC,
    DPC,
    DPD,
    DPP
}

data class Branch(
    val id: String? = "",
    val branchId: String = "",
    val name: String = "",
    val location: BranchLocation = BranchLocation(),
    val level: BranchLevel = BranchLevel.PAC,
    val leaderId: String? = null,
    val members: List<String> = emptyList()
)

data class BranchLocation(
    val subDistrict: String = "",
    val city: String = "",
    val province: String = ""
)

data class News(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val content: List<NewsContent> = emptyList(), // Menggunakan NewsContent
    val thumbnailUrl: String = "",
    val author: User = User(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val isFeatured: Boolean = false,
    val viewCount: Int = 0,
    val likes: Map<String, Boolean> = emptyMap(),
    val comments: List<String> = emptyList(),
    val commentCount: Int = 0,
    val shares: Int = 0
)


data class NewsContent(
    val text: String? = null,           // Untuk konten teks
    val imageUrl: String? = null,       // Untuk konten gambar
    val videoUrl: String? = null,       // Untuk konten video
    val videoThumbnailUrl: String? = null, // Thumbnail untuk video
    val caption: String? = null,        // Keterangan untuk gambar/video
    val articleUrl: String? = null,     // Untuk tautan artikel
    val articleTitle: String? = null    // Judul untuk tautan artikel
)

data class Comment(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null
)

data class Bookmark(
    val userId: String = "",
    val newsId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)


