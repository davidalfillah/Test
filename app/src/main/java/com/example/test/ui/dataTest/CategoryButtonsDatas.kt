package com.example.test.ui.dataTest

import androidx.annotation.DrawableRes
import com.example.test.R


data class CategoryButton(
    val title: String,
    @DrawableRes val icon: Int
)

object CategoryButtons {
    val categoryButton = listOf(
        CategoryButton("Olahraga", R.drawable.baseline_wallet_24),
        CategoryButton("Teknologi", R.drawable.baseline_history_24),
        CategoryButton("Musik", R.drawable.baseline_notifications_24),
    )
}


val categories = listOf(
    mapOf("id" to "electronics", "name" to "Elektronik"),
    mapOf("id" to "fashion", "name" to "Fashion & Aksesoris"),
    mapOf("id" to "health_beauty", "name" to "Kesehatan & Kecantikan"),
    mapOf("id" to "home_living", "name" to "Rumah & Perabotan"),
    mapOf("id" to "automotive", "name" to "Otomotif"),
    mapOf("id" to "hobbies_sports", "name" to "Hobi & Olahraga"),
    mapOf("id" to "baby_kids", "name" to "Bayi & Anak"),
    mapOf("id" to "food_beverages", "name" to "Makanan & Minuman"),
    mapOf("id" to "computers", "name" to "Komputer & Aksesoris"),
    mapOf("id" to "mobile_accessories", "name" to "Handphone & Aksesoris"),
    mapOf("id" to "books", "name" to "Buku & Alat Tulis"),
    mapOf("id" to "home_appliances", "name" to "Peralatan Rumah Tangga"),
    mapOf("id" to "pets", "name" to "Produk Hewan Peliharaan"),
    mapOf("id" to "industrial", "name" to "Industri & Bisnis"),
    mapOf("id" to "travel", "name" to "Perjalanan & Tiket"),
    mapOf("id" to "gaming", "name" to "Gaming & Konsol"),
    mapOf("id" to "digital_products", "name" to "Produk Digital"),
    mapOf("id" to "tickets_vouchers", "name" to "Tiket & Voucher"),
    mapOf("id" to "jewelry_watches", "name" to "Perhiasan & Jam Tangan")
)

val subcategories = mapOf(
    "electronics" to listOf(
        mapOf("id" to "smartphones", "name" to "Smartphone & Tablet"),
        mapOf("id" to "tv_audio", "name" to "TV & Home Audio"),
        mapOf("id" to "camera", "name" to "Kamera & Aksesoris"),
        mapOf("id" to "wearables", "name" to "Jam Tangan Pintar & Aksesoris")
    ),
    "fashion" to listOf(
        mapOf("id" to "mens_clothing", "name" to "Pakaian Pria"),
        mapOf("id" to "womens_clothing", "name" to "Pakaian Wanita"),
        mapOf("id" to "shoes", "name" to "Sepatu & Sandal"),
        mapOf("id" to "bags", "name" to "Tas & Dompet")
    ),
    "health_beauty" to listOf(
        mapOf("id" to "skincare", "name" to "Perawatan Kulit"),
        mapOf("id" to "makeup", "name" to "Kosmetik"),
        mapOf("id" to "personal_care", "name" to "Perawatan Pribadi"),
        mapOf("id" to "health_devices", "name" to "Alat Kesehatan")
    ),
    "home_living" to listOf(
        mapOf("id" to "furniture", "name" to "Furniture"),
        mapOf("id" to "home_decor", "name" to "Dekorasi Rumah"),
        mapOf("id" to "kitchenware", "name" to "Peralatan Dapur"),
        mapOf("id" to "bathroom", "name" to "Perlengkapan Kamar Mandi")
    ),
    "automotive" to listOf(
        mapOf("id" to "car_accessories", "name" to "Aksesoris Mobil"),
        mapOf("id" to "motorcycle_accessories", "name" to "Aksesoris Motor"),
        mapOf("id" to "car_parts", "name" to "Suku Cadang Mobil"),
        mapOf("id" to "motorcycle_parts", "name" to "Suku Cadang Motor")
    ),
    "baby_kids" to listOf(
        mapOf("id" to "baby_clothing", "name" to "Pakaian Bayi"),
        mapOf("id" to "toys", "name" to "Mainan Anak"),
        mapOf("id" to "strollers", "name" to "Kereta Dorong & Gendongan"),
        mapOf("id" to "baby_feeding", "name" to "Peralatan Makan Bayi")
    ),
    "food_beverages" to listOf(
        mapOf("id" to "snacks", "name" to "Makanan Ringan"),
        mapOf("id" to "beverages", "name" to "Minuman"),
        mapOf("id" to "cooking_ingredients", "name" to "Bahan Masak"),
        mapOf("id" to "fresh_food", "name" to "Makanan Segar")
    ),
    "computers" to listOf(
        mapOf("id" to "laptops", "name" to "Laptop & Aksesoris"),
        mapOf("id" to "desktops", "name" to "PC & Komponen"),
        mapOf("id" to "printers", "name" to "Printer & Scanner"),
        mapOf("id" to "networking", "name" to "Peralatan Jaringan")
    ),
    "digital_products" to listOf(
        mapOf("id" to "game_vouchers", "name" to "Voucher Game"),
        mapOf("id" to "software", "name" to "Software & Lisensi"),
        mapOf("id" to "e_books", "name" to "E-Books & Komik Digital"),
        mapOf("id" to "music_streaming", "name" to "Streaming Musik & Film")
    ),
    "jewelry_watches" to listOf(
        mapOf("id" to "mens_watches", "name" to "Jam Tangan Pria"),
        mapOf("id" to "womens_watches", "name" to "Jam Tangan Wanita"),
        mapOf("id" to "necklaces", "name" to "Kalung & Gelang"),
        mapOf("id" to "rings", "name" to "Cincin")
    )
)
