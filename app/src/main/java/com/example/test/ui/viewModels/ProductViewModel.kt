package com.example.test.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.test.ui.dataType.Dimensions
import com.example.test.ui.dataType.Product
import com.example.test.ui.dataType.ProductCategory
import com.example.test.ui.dataType.ProductMedia
import com.example.test.ui.dataType.ProductVariant
import com.example.test.ui.dataType.Subcategory
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

val product = Product(
    id = "productId1",
    name = "Smartphone XYZ",
    description = "Smartphone dengan kamera 108MP",
    categoryId = "electronics",
    subcategoryId = "smartphones",
    price = 5999000.0,
    discount = 10.0,
    stock = 50,
    weight = 0.5,
    dimensions = Dimensions(15.0, 7.5, 0.8),
    sellerId = "seller123",
    createdAt = Timestamp.now(),
    updatedAt = Timestamp.now(),
    status = "active",
    rating = 4.5,
    soldCount = 100
)

val images = listOf(
    ProductMedia(id = "media1", url = "https://example.com/image1.jpg", type = "image"),
    ProductMedia(id = "media2", url = "https://example.com/image2.jpg", type = "image"),
    ProductMedia(id = "media3", url = "https://example.com/video.mp4", type = "video")
)

val variants = listOf(
    ProductVariant(id = "variant1", name = "Hitam - XL", price = 5999000.0, stock = 10),
    ProductVariant(id = "variant2", name = "Putih - L", price = 5999000.0, stock = 15)
)

class ProductViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

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



    fun addProductToFirestore(product: Product, images: List<ProductMedia>, variants: List<ProductVariant>) {
        val firestore = FirebaseFirestore.getInstance()
        val productRef = firestore.collection("products").document() // Auto-generate ID
        val productId = productRef.id

        // Simpan produk dengan ID yang di-generate
        val productWithId = product.copy(id = productId)
        productRef.set(productWithId)
            .addOnSuccessListener {
                Log.d("Firestore", "Produk berhasil ditambahkan dengan ID: $productId")

                // Upload Media (ID Otomatis)
                images.forEach { media ->
                    val mediaRef = productRef.collection("media").document() // Auto-generate ID
                    val mediaWithId = media.copy(id = mediaRef.id)
                    mediaRef.set(mediaWithId)
                }

                // Upload Variants (ID Otomatis)
                variants.forEach { variant ->
                    val variantRef = productRef.collection("variants").document() // Auto-generate ID
                    val variantWithId = variant.copy(id = variantRef.id)
                    variantRef.set(variantWithId)
                }

            }
            .addOnFailureListener {
                Log.e("Firestore", "Gagal menambahkan produk", it)
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