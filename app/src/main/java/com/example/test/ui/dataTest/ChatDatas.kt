package com.example.test.ui.dataTest

import com.example.test.R

data class ChatItem(
    val profilePic: String, // URL atau drawable
    val name: String, // Nama pengguna
    val lastMessage: String, // Pesan terakhir
    val time: String, // Waktu terakhir
    val badge: Int // Jumlah pesan belum dibaca
)



val sampleChats = listOf(
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/men/1.jpg",
        name = "David Alfillah",
        lastMessage = "Hey, apa kabar?",
        time = "10:30 AM",
        badge = 2
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/women/2.jpg",
        name = "Aulia Rahma",
        lastMessage = "Aku sudah mengirim dokumen.",
        time = "9:15 AM",
        badge = 0
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/men/3.jpg",
        name = "Budi Santoso",
        lastMessage = "Besok kita jadi meeting?",
        time = "Yesterday",
        badge = 5
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/women/4.jpg",
        name = "Citra Dewi",
        lastMessage = "Oke, nanti aku kabari lagi.",
        time = "Yesterday",
        badge = 1
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/men/5.jpg",
        name = "Eko Prasetyo",
        lastMessage = "Haha, lucu banget!",
        time = "Monday",
        badge = 3
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/women/6.jpg",
        name = "Fitri Handayani",
        lastMessage = "Jangan lupa bawa tiket ya.",
        time = "Sunday",
        badge = 0
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/men/7.jpg",
        name = "Gilang Saputra",
        lastMessage = "Kita berangkat jam berapa?",
        time = "Saturday",
        badge = 4
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/women/8.jpg",
        name = "Hana Lestari",
        lastMessage = "Aku sudah sampai di lokasi.",
        time = "Friday",
        badge = 2
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/men/9.jpg",
        name = "Iqbal Mahendra",
        lastMessage = "Nanti aku konfirmasi lagi ya.",
        time = "Thursday",
        badge = 1
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/women/10.jpg",
        name = "Jessica Anindya",
        lastMessage = "Aku sudah pesan makanannya.",
        time = "Wednesday",
        badge = 3
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/men/11.jpg",
        name = "Kevin Setiawan",
        lastMessage = "Oke, nanti aku jemput kamu.",
        time = "Tuesday",
        badge = 0
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/women/12.jpg",
        name = "Lisa Ramadhani",
        lastMessage = "Kita ketemu di tempat biasa ya.",
        time = "Monday",
        badge = 5
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/men/13.jpg",
        name = "Mikhael Pratama",
        lastMessage = "Jangan lupa bawa dokumennya.",
        time = "Sunday",
        badge = 2
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/women/14.jpg",
        name = "Nadya Salsabila",
        lastMessage = "Makasih banyak ya!",
        time = "Saturday",
        badge = 1
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/men/15.jpg",
        name = "Oscar Fadillah",
        lastMessage = "Aku di jalan sekarang.",
        time = "Friday",
        badge = 3
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/women/16.jpg",
        name = "Putri Amanda",
        lastMessage = "Bentar lagi aku sampai.",
        time = "Thursday",
        badge = 0
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/men/17.jpg",
        name = "Rizky Darmawan",
        lastMessage = "Bisa tolong belikan minum?",
        time = "Wednesday",
        badge = 4
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/women/18.jpg",
        name = "Siti Zulaikha",
        lastMessage = "Nanti aku hubungi lagi.",
        time = "Tuesday",
        badge = 2
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/men/19.jpg",
        name = "Taufik Hidayat",
        lastMessage = "Sampai ketemu besok!",
        time = "Monday",
        badge = 0
    ),
    ChatItem(
        profilePic = "https://randomuser.me/api/portraits/women/20.jpg",
        name = "Umi Fatimah",
        lastMessage = "Siap, aku catat ya.",
        time = "Sunday",
        badge = 1
    )
)

