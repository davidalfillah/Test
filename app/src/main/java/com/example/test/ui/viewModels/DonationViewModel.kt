package com.example.test.ui.viewModels


import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject


// Callback untuk operasi donasi
interface DonationCallback {
    fun onSuccess(donations: List<Donation>)
    fun onError(error: String)
}

// Callback untuk transaksi
interface TransactionCallback {
    fun onTransactionSuccess(transactionId: String)
    fun onTransactionError(error: String)
}

@Keep
data class Donation(
    val id: String,
    val title: String = "",
    val thumbnailUrl: String = "",
    val category: String = "",
    val content: List<ContentItem> = emptyList(),
    val totalCollected: Long = 0L,
    val targetAmount: Long? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val lastUpdated: Timestamp = Timestamp.now(),
    val verified: Boolean = false,
    val deadline: Timestamp? = null
) {

    constructor() : this("","", "", "", emptyList(), 0L, null, Timestamp.now(), Timestamp.now(), false, null)
}

@Keep
data class ContentItem(
    val type: String = "", // "text", "image", "link"
    val value: String = ""
) {
    constructor() : this("", "")
}

class DonationViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    fun fetchDonations(
        category: String,
        onSuccess: (List<Donation>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val query = if (category == "Semua") {
                db.collection("donations").whereEqualTo("verified", true)
            } else {
                db.collection("donations").whereEqualTo("category", category)
                    .whereEqualTo("verified", true)
            }

            query.get()
                .addOnSuccessListener { documents ->
                    val donationList = documents.mapNotNull { document ->
                        val donation =
                            document.toObject<Donation>()?.copy(id = document.id) // Tambahkan ID
                        if (donation?.deadline == null || donation.deadline.toDate()
                                .after(java.util.Date())
                        ) {
                            donation
                        } else {
                            null
                        }
                    }
                    onSuccess(donationList)
                }
                .addOnFailureListener { exception ->
                    onFailure("Failed to fetch donations: ${exception.message}")
                }
        } catch (e: Exception) {
            onFailure("Unexpected error: ${e.localizedMessage}")
        }
    }


    fun getDonationById(
        donationId: String,
        onSuccess: (Donation) -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("donations")
            .document(donationId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val donation = document.toObject(Donation::class.java)
                    donation?.let { onSuccess(it) } ?: onFailure("Data tidak valid")
                } else {
                    onFailure("Donasi tidak ditemukan")
                }
            }
            .addOnFailureListener { e ->
                onFailure("Error: ${e.localizedMessage}")
            }
    }




}


