package com.example.test.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val category: String = "",
    val amount: Long = 0L,
    val status: String = "PENDING",
    val paymentUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class PaymentViewModel : ViewModel() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
    private var transactionListener: ListenerRegistration? = null

    interface Callback {
        fun onLoading(isLoading: Boolean)
        fun onError(message: String?)
        fun onTransactionsUpdated(transactions: List<Transaction>)
    }

    private var callback: Callback? = null

    fun setCallback(callback: Callback, userId: String) {
        this.callback = callback
        listenToTransactions(userId)
    }

    fun createPayment(category: String, amount: Long, userId: String,memberId:String,referenceId:String, email: String, phone: String) {
        viewModelScope.launch {
            callback?.onLoading(true)
            callback?.onError(null)
            try {
                val data = mapOf(
                    "amount" to amount,
                    "category" to category,
                    "userId" to userId,
                    "email" to email,
                    "phone" to phone,
                    "memberId" to memberId,
                    "referenceId" to referenceId
                )
                println("Data dikirim ke Firebase Functions: $data")
                val result = functions
                    .getHttpsCallable("payment-create")
                    .call(data)
                    .await()
                val response = result.data as Map<*, *>
                if (response["success"] as Boolean) {
                    val transactionData = response["data"] as Map<*, *>
                    callback?.onError("Silakan selesaikan pembayaran: ${transactionData["paymentUrl"]}")
                } else {
                    throw Exception(response["message"] as String)
                }
            } catch (e: Exception) {
                callback?.onError("Gagal membuat transaksi: ${e.message}")
            } finally {
                callback?.onLoading(false)
            }
        }
    }

    private fun listenToTransactions(userId: String) {
        transactionListener?.remove()
        transactionListener = db.collection("transactions")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    callback?.onError("Gagal memuat transaksi: ${error.message}")
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val transactions = it.toObjects(Transaction::class.java)
                    callback?.onTransactionsUpdated(transactions)
                }
            }
    }

    override fun onCleared() {
        transactionListener?.remove()
        callback = null
        super.onCleared()
    }
}