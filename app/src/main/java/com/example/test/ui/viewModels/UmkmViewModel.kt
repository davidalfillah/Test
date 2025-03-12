package com.example.test.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.ui.dataType.Address
import com.example.test.ui.dataType.Umkm
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


// ViewModel untuk UMKM
class UmkmViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        getUmkmData()
    }

    private val _umkmList = MutableStateFlow<List<Umkm>>(emptyList())
    val umkmList = _umkmList.asStateFlow()

    fun getUmkmData(callback: ((List<Umkm>) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                Log.d("UmkmViewModel", "Starting to fetch UMKM data")
                val snapshot = firestore.collection("umkm")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                Log.d("UmkmViewModel", "Got ${snapshot.size()} documents")
                
                val parsedUmkmList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            val addressMap = data["address"] as? Map<String, Any>
                            val address = Address(
                                street = addressMap?.get("street") as? String ?: "",
                                city = addressMap?.get("city") as? String ?: "",
                                province = addressMap?.get("province") as? String ?: "",
                                postalCode = addressMap?.get("postalCode") as? String ?: ""
                            )

                            Umkm(
                                umkmId = doc.id,
                                ownerId = data["memberId"] as? String ?: "",
                                name = data["name"] as? String ?: "",
                                businessType = data["businessType"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                address = address,
                                contact = data["contact"] as? String ?: "",
                                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                                status = data["status"] as? String ?: "active"
                            )
                        } else null
                    } catch (e: Exception) {
                        Log.e("UmkmViewModel", "Error parsing UMKM document ${doc.id}: ${e.message}")
                        null
                    }
                }
               
                Log.d("UmkmViewModel", "Successfully parsed ${parsedUmkmList.size} UMKM")
                _umkmList.value = parsedUmkmList
                callback?.invoke(parsedUmkmList)
            } catch (e: Exception) {
                Log.e("UmkmViewModel", "Error fetching UMKM data: ${e.message}")
                _error.value = "Gagal memuat data UMKM: ${e.message}"
                callback?.invoke(emptyList())
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData(callback: ((List<Umkm>) -> Unit)? = null) {
        getUmkmData(callback)
    }

    fun getUmkmByOwnerId(ownerId: String, callback: (List<Umkm>) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("UmkmViewModel", "Fetching UMKM for owner: $ownerId")
                val snapshot = firestore.collection("umkm")
                    .whereEqualTo("ownerId", ownerId)
                    .get()
                    .await()

                Log.d("UmkmViewModel", "Found ${snapshot.size()} UMKM for owner")
               
                val umkmList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            val addressMap = data["address"] as? Map<String, Any>
                            val address = Address(
                                street = addressMap?.get("street") as? String ?: "",
                                city = addressMap?.get("city") as? String ?: "",
                                province = addressMap?.get("province") as? String ?: "",
                                postalCode = addressMap?.get("postalCode") as? String ?: ""
                            )

                            Umkm(
                                umkmId = doc.id,
                                ownerId = data["ownerId"] as? String ?: "",
                                name = data["name"] as? String ?: "",
                                businessType = data["businessType"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                address = address,
                                contact = data["contact"] as? String ?: "",
                                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                                status = data["status"] as? String ?: "active"
                            )
                        } else null
                    } catch (e: Exception) {
                        Log.e("UmkmViewModel", "Error parsing UMKM document: ${e.message}")
                        null
                    }
                }
                callback(umkmList)
            } catch (e: Exception) {
                Log.e("UmkmViewModel", "Error fetching UMKM by owner: ${e.message}")
                callback(emptyList())
            }
        }
    }

    fun hasUmkm(memberId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("umkm")
                    .whereEqualTo("ownerId", memberId)
                    .limit(1)
                    .get()
                    .await()
               
                val hasUmkm = !snapshot.isEmpty
                Log.d("UmkmViewModel", "Checking if member $memberId has UMKM: $hasUmkm")
                callback(hasUmkm)
            } catch (e: Exception) {
                Log.e("UmkmViewModel", "Error checking UMKM ownership: ${e.message}")
                callback(false)
            }
        }
    }

    fun getUmkmIds(callback: (List<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("umkm").get().await()
                Log.d("UmkmViewModel", "Raw Firestore response for IDs: ${snapshot.documents.map { it.id }}")
                
                // Change: Using document IDs instead of ownerId field
                val umkmIds = snapshot.documents.map { it.id }
                Log.d("UmkmViewModel", "Found UMKM IDs: $umkmIds")
                
                callback(umkmIds)
            } catch (e: Exception) {
                Log.e("UmkmViewModel", "Error fetching UMKM IDs: ${e.message}")
                e.printStackTrace()
                callback(emptyList())
            }
        }
    }

    fun getUmkmDetail(umkmId: String, callback: ((Umkm?) -> Unit)) {
        viewModelScope.launch {
            try {
                Log.d("UmkmViewModel", "Fetching UMKM detail for ID: $umkmId")
                val docSnapshot = firestore.collection("umkm")
                    .document(umkmId)
                    .get()
                    .await()

                if (docSnapshot.exists()) {
                    val data = docSnapshot.data
                    if (data != null) {
                        val addressMap = data["address"] as? Map<String, Any>
                        val address = Address(
                            street = addressMap?.get("street") as? String ?: "",
                            village = addressMap?.get("village") as? String ?: "",
                            subDistrict = addressMap?.get("subDistrict") as? String ?: "",
                            city = addressMap?.get("city") as? String ?: "",
                            province = addressMap?.get("province") as? String ?: "",
                            postalCode = addressMap?.get("postalCode") as? String ?: ""
                        )

                        val umkm = Umkm(
                            umkmId = docSnapshot.id,
                            ownerId = data["ownerId"] as? String ?: "",
                            imageUrl = data["imageUrl"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            businessType = data["businessType"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            address = address,
                            contact = data["contact"] as? String ?: "",
                            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                            status = data["status"] as? String ?: "active"
                        )
                        Log.d("UmkmViewModel", "Successfully fetched UMKM detail")
                        callback(umkm)
                    } else {
                        callback(null)
                    }
                } else {
                    Log.d("UmkmViewModel", "UMKM not found")
                    callback(null)
                }
            } catch (e: Exception) {
                Log.e("UmkmViewModel", "Error fetching UMKM detail: ${e.message}")
                callback(null)
            }
        }
    }
}