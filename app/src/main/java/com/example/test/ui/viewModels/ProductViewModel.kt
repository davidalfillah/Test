package com.example.test.ui.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.test.ui.dataType.Product
import com.example.test.ui.dataType.ProductCategory
import com.example.test.ui.dataType.ProductMedia
import com.example.test.ui.dataType.ProductVariant
import com.example.test.ui.dataType.Subcategory
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")

    fun fetchCategories(callback: (List<ProductCategory>) -> Unit) {
        db.collection("productCategories")
            .get()
            .addOnSuccessListener { documents ->
                val categoryList = mutableListOf<ProductCategory>()
                val totalDocs = documents.size()
                var completedDocs = 0

                if (totalDocs == 0) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                // Iterasi setiap dokumen kategori
                for (doc in documents) {
                    val categoryId = doc.id
                    val categoryName = doc.getString("name") ?: ""

                    // Mengambil subkoleksi "subcategories" untuk kategori ini
                    db.collection("productCategories")
                        .document(categoryId)
                        .collection("subcategories")
                        .get()
                        .addOnSuccessListener { subDocs ->
                            val subcategories = subDocs.map { subDoc ->
                                Subcategory(
                                    id = subDoc.id,
                                    name = subDoc.getString("name") ?: ""
                                )
                            }

                            // Buat objek ProductCategory
                            val category = ProductCategory(
                                id = categoryId,
                                name = categoryName,
                                subcategories = subcategories
                            )
                            categoryList.add(category)

                            // Tambah counter, jika semua dokumen selesai, panggil callback
                            completedDocs++
                            if (completedDocs == totalDocs) {
                                Log.d("categoryList", categoryList.toString())
                                callback(categoryList)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Gagal mengambil subkategori untuk $categoryId", e)
                            // Tambahkan kategori tanpa subkategori jika gagal
                            categoryList.add(ProductCategory(categoryId, categoryName, emptyList()))
                            completedDocs++
                            if (completedDocs == totalDocs) {
                                Log.d("categoryList", categoryList.toString())
                                callback(categoryList)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Gagal mengambil kategori", e)
                callback(emptyList())
            }
    }

    fun fetchSubcategories(categoryId: String, callback: (List<Subcategory>) -> Unit) {
        db.collection("productCategories")
            .document(categoryId)
            .collection("subcategories")
            .get()
            .addOnSuccessListener { subDocs ->
                val subcategories = subDocs.map { subDoc ->
                    Subcategory(
                        id = subDoc.id,
                        name = subDoc.getString("name") ?: ""
                    )
                }.toList()
                callback(subcategories)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Gagal mengambil subkategori", e)
                callback(emptyList())
            }
    }

    suspend fun addProduct(product: Product, mediaUris: List<Uri>): Result<String> {
        return try {
            val storage = FirebaseStorage.getInstance()
            val mediaRefs = mutableListOf<ProductMedia>()
            val productId = UUID.randomUUID().toString()
            val timestamp = Timestamp.now()

            // Upload media files
            var thumbnailUrl = "" // Untuk menyimpan URL thumbnail

            // Upload dan proses media
            mediaUris.mapIndexed { index, uri ->
                val mediaId = UUID.randomUUID().toString()
                val mediaRef = storage.reference.child("products/$productId/$mediaId")

                mediaRef.putFile(uri).await()
                val downloadUrl = mediaRef.downloadUrl.await().toString()

                val media = ProductMedia(
                    id = mediaId,
                    url = downloadUrl,
                    type = if (uri.toString().endsWith(".mp4")) "video" else "image",
                    timestamp = timestamp
                )

                // Simpan media reference
                db.collection("products")
                    .document(productId)
                    .collection("media")
                    .document(mediaId)
                    .set(media)
                    .await()

                mediaRefs.add(media)

                // Gunakan media pertama sebagai thumbnail
                if (index == 0) {
                    thumbnailUrl = downloadUrl
                }
            }

            // Create product dengan thumbnail
            val productWithDetails = product.copy(
                id = productId,
                thumbnail = thumbnailUrl, // Tambahkan thumbnail URL
                createdAt = timestamp,
                updatedAt = timestamp
            )

            // Simpan data produk
            db.collection("products")
                .document(productId)
                .set(productWithDetails)
                .await()

            // Proses variants
            product.variants.map { variant ->
                val variantId = UUID.randomUUID().toString()
                val variantWithId = variant.copy(id = variantId)

                db.collection("products")
                    .document(productId)
                    .collection("variants")
                    .document(variantId)
                    .set(variantWithId)
                    .await()
            }

            Result.success(productId)
        } catch (e: Exception) {
            Log.e("ProductViewModel", "Error adding product: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun fetchProductsByLocation(
        userLat: Double,
        userLong: Double,
        maxDistance: Double = 10.0, // dalam kilometer
        onSuccess: (List<Product>) -> Unit,
        onError: (String) -> Unit
    ) {
        productsCollection
            .get()
            .addOnSuccessListener { documents ->
                try {
                    val products = documents.mapNotNull { doc ->
                        doc.toObject(Product::class.java)
                    }

                    // Hitung jarak dan filter produk
                    val filteredAndSortedProducts = products
                        .map { product ->
                            // Hitung jarak antara user dan produk
                            val distance = calculateDistance(
                                userLat, userLong,
                                product.location.latitude,
                                product.location.longitude
                            )
                            Pair(product, distance)
                        }
                        .filter { (_, distance) ->
                            distance <= maxDistance // Filter berdasarkan jarak maksimum
                        }
                        .sortedBy { (_, distance) ->
                            distance // Urutkan berdasarkan jarak terdekat
                        }
                        .map { (product, _) -> product }

                    onSuccess(filteredAndSortedProducts)
                } catch (e: Exception) {
                    onError(e.message ?: "Terjadi kesalahan")
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Gagal mengambil data produk")
            }
    }

    // Fungsi untuk menghitung jarak menggunakan formula Haversine
    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371 // radius bumi dalam kilometer
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    // Fungsi untuk mendapatkan produk dengan pagination dan filter lokasi
    fun fetchProductsByLocationPaginated(
        userLat: Double,
        userLong: Double,
        maxDistance: Double = 10.0,
        lastProduct: Product? = null,
        pageSize: Long = 10,
        onSuccess: (List<Product>) -> Unit,
        onError: (String) -> Unit
    ) {
        var query = productsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(pageSize)

        if (lastProduct != null) {
            query = query.startAfter(lastProduct.createdAt)
        }

        query.get()
            .addOnSuccessListener { documents ->
                try {
                    // Membuat list untuk menyimpan semua Future
                    val productFutures = documents.mapNotNull { doc ->
                        val product = doc.toObject(Product::class.java)
                        // Ambil media untuk setiap produk
                        doc.reference.collection("media")
                            .limit(1) // Ambil hanya 1 media untuk thumbnail
                            .get()
                            .continueWith { mediaSnapshot ->
                                if (!mediaSnapshot.isSuccessful) {
                                    return@continueWith product
                                }

                                val media = mediaSnapshot.result?.documents?.firstOrNull()
                                    ?.toObject(ProductMedia::class.java)

                                // Update product dengan thumbnail dari media pertama
                                if (media != null) {
                                    product.copy(thumbnail = media.url)
                                } else {
                                    product
                                }
                            }
                    }

                    // Tunggu semua Future selesai
                    Tasks.whenAllComplete(productFutures)
                        .addOnSuccessListener {
                            // Ambil hasil dan filter yang sukses
                            val products = productFutures.mapNotNull { future ->
                                future.result as? Product
                            }

                            // Filter dan sort berdasarkan lokasi
                            val filteredAndSortedProducts = products
                                .map { product ->
                                    val distance = calculateDistance(
                                        userLat, userLong,
                                        product.location.latitude,
                                        product.location.longitude
                                    )
                                    Pair(product, distance)
                                }
                                .filter { (_, distance) ->
                                    distance <= maxDistance
                                }
                                .sortedBy { (_, distance) ->
                                    distance
                                }
                                .map { (product, _) -> product }

                            onSuccess(filteredAndSortedProducts)
                        }
                        .addOnFailureListener { e ->
                            onError(e.message ?: "Gagal memproses data produk")
                        }

                } catch (e: Exception) {
                    onError(e.message ?: "Terjadi kesalahan")
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Gagal mengambil data produk")
            }
    }

    fun fetchRandomProducts(
        limit: Long = 10,
        onSuccess: (List<Product>) -> Unit,
        onError: (String) -> Unit
    ) {
        // Ambil semua produk terlebih dahulu
        productsCollection
            .get()
            .addOnSuccessListener { documents ->
                try {
                    // Konversi documents ke list dan acak urutannya
                    val allProducts = documents.toList().shuffled()

                    // Ambil sejumlah produk sesuai limit
                    val randomDocs = allProducts.take(limit.toInt())

                    // Proses untuk mendapatkan thumbnail dari subcollection media
                    val productFutures = randomDocs.mapNotNull { doc ->
                        val product = doc.toObject(Product::class.java)
                        doc.reference.collection("media")
                            .limit(1)
                            .get()
                            .continueWith { mediaSnapshot ->
                                if (!mediaSnapshot.isSuccessful) {
                                    return@continueWith product
                                }

                                val media = mediaSnapshot.result?.documents?.firstOrNull()
                                    ?.toObject(ProductMedia::class.java)

                                if (media != null) {
                                    product.copy(thumbnail = media.url)
                                } else {
                                    product
                                }
                            }
                    }

                    // Tunggu semua Future selesai
                    Tasks.whenAllComplete(productFutures)
                        .addOnSuccessListener {
                            val products = productFutures.mapNotNull { future ->
                                future.result as? Product
                            }
                            onSuccess(products)
                        }
                        .addOnFailureListener { e ->
                            onError(e.message ?: "Gagal memproses data produk")
                        }

                } catch (e: Exception) {
                    onError(e.message ?: "Terjadi kesalahan")
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Gagal mengambil data produk")
            }
    }

    fun fetchMyProducts(
        onLoading: () -> Unit = {},
        onSuccess: (List<Product>) -> Unit,
        onError: (String) -> Unit
    ) {
        onLoading()

        // Dapatkan current user ID
        val currentUserId = Firebase.auth.currentUser?.uid ?: run {
            onError("User tidak ditemukan")
            return
        }

        productsCollection
            .whereEqualTo("sellerId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                // Proses untuk mendapatkan thumbnail dari subcollection media
                val productFutures = documents.mapNotNull { doc ->
                    val product = doc.toObject(Product::class.java)
                    doc.reference.collection("media")
                        .limit(1)
                        .get()
                        .continueWith { mediaSnapshot ->
                            if (!mediaSnapshot.isSuccessful) {
                                return@continueWith product
                            }

                            val media = mediaSnapshot.result?.documents?.firstOrNull()
                                ?.toObject(ProductMedia::class.java)

                            if (media != null) {
                                product.copy(thumbnail = media.url)
                            } else {
                                product
                            }
                        }
                }

                Tasks.whenAllComplete(productFutures)
                    .addOnSuccessListener {
                        val products = productFutures.mapNotNull { future ->
                            future.result as? Product
                        }
                        onSuccess(products)
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Gagal memproses data produk")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Gagal mengambil data produk")
            }
    }

    fun deleteProduct(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        productsCollection.document(productId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Gagal menghapus produk")
            }
    }

    fun addCategoryToFirestore(
        categories: List<Map<String, Any>>,
        subcategories: Map<String, List<Map<String, Any>>>
    ) {

        categories.forEach { category ->
            val productRef = db.collection("productCategories").document()

            db.collection("productCategories")
                .document(productRef.id)
                .set(category)
                .addOnSuccessListener {
                    Log.d("Firestore", "Kategori ${category["name"]} berhasil diunggah!")

                    subcategories[productRef.id]?.forEach { subcategory ->

                        val productRefSub = db.collection("subcategories").document()
                        db.collection("productCategories")
                            .document(productRef.id)
                            .collection("subcategories")
                            .document(productRefSub.id)
                            .set(subcategory)
                            .addOnSuccessListener {
                                Log.d(
                                    "Firestore",
                                    "Subkategori ${subcategory["name"]} berhasil diunggah!"
                                )
                            }
                            .addOnFailureListener {
                                Log.e(
                                    "Firestore",
                                    "Gagal unggah subkategori ${subcategory["name"]}",
                                    it
                                )
                            }
                    }
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Gagal unggah kategori ${category["name"]}", it)
                }
        }

    }

}