package com.example.test.ui.viewModels

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Ad(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val actionType: String = "",
    val actionValue: String = "",
    val clicks: Long = 0,
    val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null
)


class AdViewModel {
    fun getAds(onResult: (List<Ad>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("ads")
            .orderBy("createdAt", Query.Direction.DESCENDING) // Urutkan berdasarkan waktu pembuatan
            .get()
            .addOnSuccessListener { result ->
                val adsList = result.documents.mapNotNull { it.toObject(Ad::class.java) }
                onResult(adsList) // Kirim data ke UI
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Gagal mengambil iklan", e)
                onResult(emptyList()) // Jika gagal, kirim list kosong
            }
    }

}