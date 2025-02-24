package com.example.test.ui.dataTest

import com.example.test.R
import com.example.test.ui.screens.Donation

data class ChatItem(
    val profilePic: String, // URL atau drawable
    val name: String, // Nama pengguna
    val lastMessage: String, // Pesan terakhir
    val time: String, // Waktu terakhir
    val badge: Int // Jumlah pesan belum dibaca
)



val sampleChats = listOf(
    ChatItem("https://thebluegrasssituation.com/wp-content/uploads/2020/08/Anna-Square-Headshot-1240x1240.jpg", "Alice", "Halo, bagaimana kabarmu?", "10:30 AM", 3),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),
    ChatItem("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjdw50eMlvhzjI0qQ1JYEyMReTnb7rC-igYyswilEumB8PSAP5Iq3sfFCYYWZqCy6CU0M&usqp=CAU", "Bob", "Lihat dokumen yang saya kirimkan", "09:15 AM", 0),
    ChatItem("https://plus.unsplash.com/premium_photo-1689568158814-3b8e9c1a9618?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8cGVyc29uYXxlbnwwfHwwfHx8MA%3D%3D", "Charlie", "Baik, terima kasih!", "Yesterday", 1),

)