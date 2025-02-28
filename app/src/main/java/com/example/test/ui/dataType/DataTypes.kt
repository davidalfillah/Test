package com.example.test.ui.dataType

import androidx.annotation.Keep
import com.example.test.ui.screens.User
import com.google.firebase.Timestamp
import java.sql.Time

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
    val phone: String = "",
    val job: String = "",
    val address: Address = Address(),
    val branchId: String = "",
    val branchLevel: BranchLevel = BranchLevel.PAC,
    val jobTitle: String = "Anggota",
    val status: String = "active",
    val createdAt: Timestamp = Timestamp.now(),
    val umkmIds: List<String> = emptyList(), // List ID UMKM yang dimiliki oleh anggota
)

data class Umkm(
    val umkmId: String = "",  // ID unik untuk UMKM
    val ownerId: String = "", // ID anggota yang memiliki UMKM (memberId)
    val name: String = "", // Nama UMKM
    val businessType: String = "", // Jenis usaha (contoh: Kuliner, Fashion, dll.)
    val description: String = "", // Deskripsi usaha
    val address: Address = Address(), // Alamat UMKM
    val contact: String = "", // Kontak UMKM (Nomor HP / Email)
    val registrationDate: Long = System.currentTimeMillis(), // Tanggal pendaftaran
    val status: String = "active" // Status usaha (active, inactive)
)



enum class BranchLevel {
    PAC, // Pimpinan Anak Cabang (Kecamatan)
    DPC, // Dewan Pimpinan Cabang (Kabupaten/Kota)
    DPD, // Dewan Pimpinan Daerah (Provinsi)
    DPP  // Dewan Pimpinan Pusat
}

data class Branch(
    val id: String? = "",
    val branchId: String = "",
    val name: String = "",
    val location: BranchLocation = BranchLocation(),
    val level: BranchLevel = BranchLevel.PAC, // Default PAC (Kecamatan)
    val leaderId: String? = null,  // ID anggota yang menjadi ketua cabang
    val members: List<String> = emptyList()  // List ID anggota dalam cabang
)

data class BranchLocation(
    val subDistrict: String = "", // Kecamatan (PAC)
    val city: String = "", // Kabupaten/Kota (DPC)
    val province: String = "" // Provinsi (DPD)
)


