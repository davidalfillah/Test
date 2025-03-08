package com.example.test.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algolia.client.api.SearchClient
import com.algolia.client.model.search.SearchForHits
import com.algolia.client.model.search.SearchMethodParams
import com.algolia.client.model.search.SearchResponse
import com.algolia.client.model.search.SearchResponses // Impor eksplisit
import com.example.test.ui.dataType.Bookmark
import com.example.test.ui.dataType.Comment
import com.example.test.ui.dataType.News
import com.example.test.ui.dataType.NewsContent
import com.example.test.ui.screens.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsViewModel : ViewModel() {
    private val client = SearchClient(
        appId = "Q77GNR02CJ",
        apiKey = "f930f85e418b785e53a57aa4c7c25232",
    )
    private val indexName = "newsSearch"

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val newsCollection = db.collection("news")
    private val usersCollection = db.collection("users")
    private val listeners = mutableListOf<ListenerRegistration>()

    fun migrateNewsData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("news")
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    val news = doc.toObject(News::class.java) ?: return@forEach
                    val keywords = news.title
                        .lowercase()
                        .split("\\s+".toRegex())
                        .map { it.trim().replace("[^a-zA-Z0-9]".toRegex(), "") }
                        .filter { it.isNotBlank() }
                    db.collection("news").document(news.id)
                        .update("searchKeywords", keywords)
                        .addOnSuccessListener { println("Updated ${news.id}") }
                        .addOnFailureListener { e -> println("Error updating ${news.id}: $e") }
                }
            }
    }

    fun searchNews(
        query: String,
        onLoading: () -> Unit = {},
        onSuccess: (List<News>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (query.isBlank()) {
            onSuccess(emptyList())
            return
        }

        onLoading()
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    client.search(
                        SearchMethodParams(
                            requests = listOf(
                                SearchForHits(
                                    indexName = indexName,
                                    query = query,
                                    hitsPerPage = 20
                                )
                            )
                        )
                    )
                }

                println("Full response: $response")
                val firstResult = response.results.firstOrNull()
                println("First result: $firstResult")

                val hits = (firstResult as? SearchResponse)?.hits ?: emptyList()
                println("Hits: $hits")

                val newsList = hits.mapNotNull { hit ->
                    try {
                        val data = hit.highlightResult as? Map<String, Any> ?: return@mapNotNull null
                        val id = hit.objectID ?: return@mapNotNull null

                        // Gunakan highlighted title dari highlightResult, fallback ke data["title"]
                        val title = hit.highlightResult?.get("title")?.toString()
                            ?: data["title"]?.toString() ?: ""

                        val category = data["category"]?.toString() ?: ""
                        val thumbnailUrl = data["thumbnailUrl"]?.toString() ?: ""
                        val createdAtMillis = data["createdAt"]?.toString()?.toLongOrNull()?.let {
                            Timestamp(it / 1000, ((it % 1000) * 1000000).toInt())
                        }

                        // Parsing content ke List<NewsContent>
                        val contentList = (data["content"] as? List<Map<String, Any>>)?.mapNotNull { contentItem ->
                            NewsContent(
                                text = contentItem["text"]?.toString(),
                                imageUrl = contentItem["imageUrl"]?.toString(),
                                videoUrl = contentItem["videoUrl"]?.toString(),
                                videoThumbnailUrl = contentItem["videoThumbnailUrl"]?.toString(),
                                caption = contentItem["caption"]?.toString(),
                                articleUrl = contentItem["articleUrl"]?.toString(),
                                articleTitle = contentItem["articleTitle"]?.toString()
                            )
                        } ?: emptyList()

                        // Parsing author ke User
                        val authorMap = data["author"] as? Map<String, Any>
                        val author = User(
                            uid = authorMap?.get("uid")?.toString() ?: "",
                            name = authorMap?.get("name")?.toString() ?: "",
                            profilePicUrl = authorMap?.get("profilePicUrl")?.toString() ?: "",
                            phone = authorMap?.get("phone")?.toString() ?: "",
                            role = authorMap?.get("role")?.toString() ?: "user",
                        )

                        News(
                            id = id,
                            title = title,
                            category = category,
                            content = contentList,
                            thumbnailUrl = thumbnailUrl,
                            author = author,
                            createdAt = createdAtMillis
                        )
                    } catch (e: Exception) {
                        println("Error parsing hit: $e")
                        null
                    }
                }
                onSuccess(newsList)
            } catch (e: Exception) {
                onError(e.message ?: "Gagal mencari berita")
            }
        }
    }

    fun fetchLatestNews(
        limit: Long = 5,
        onLoading: () -> Unit = {},
        onSuccess: (List<News>) -> Unit,
        onError: (String) -> Unit
    ) {
        onLoading()
        val listener = newsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Gagal mengambil berita terbaru")
                    return@addSnapshotListener
                }
                val newsList = snapshot?.documents?.mapNotNull { it.toObject(News::class.java) } ?: emptyList()
                onSuccess(newsList)
            }
        listeners.add(listener)
    }

    fun fetchTrendingNews(
        limit: Long = 5,
        onLoading: () -> Unit = {},
        onSuccess: (List<News>) -> Unit,
        onError: (String) -> Unit
    ) {
        onLoading()
        val listener = newsCollection
            .orderBy("viewCount", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Gagal mengambil berita viral")
                    return@addSnapshotListener
                }
                val newsList = snapshot?.documents?.mapNotNull { it.toObject(News::class.java) } ?: emptyList()
                onSuccess(newsList)
            }
        listeners.add(listener)
    }

    fun fetchFeaturedNews(
        limit: Long = 5,
        onLoading: () -> Unit = {},
        onSuccess: (List<News>) -> Unit,
        onError: (String) -> Unit
    ) {
        onLoading()
        val listener = newsCollection
            .whereEqualTo("isFeatured", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Gagal mengambil berita unggulan")
                    return@addSnapshotListener
                }
                val newsList = snapshot?.documents?.mapNotNull { it.toObject(News::class.java) } ?: emptyList()
                onSuccess(newsList)
            }
        listeners.add(listener)
    }

    fun fetchRelatedNews(
        currentNewsId: String, // ID berita yang sedang dilihat
        category: String,      // Kategori berita saat ini
        limit: Long = 5,
        onLoading: () -> Unit = {},
        onSuccess: (List<News>) -> Unit,
        onError: (String) -> Unit
    ) {
        onLoading()
        val listener = newsCollection
            .whereEqualTo("category", category)
            .whereNotEqualTo("id", currentNewsId)
            .orderBy("viewCount", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Gagal mengambil artikel terkait")
                    return@addSnapshotListener
                }
                val newsList = snapshot?.documents?.mapNotNull { it.toObject(News::class.java) } ?: emptyList()
                onSuccess(newsList)
            }
        listeners.add(listener)
    }

    /** Mengambil Semua Berita */
    fun fetchNews(
        onLoading: () -> Unit = {},
        onSuccess: (List<News>) -> Unit,
        onError: (String) -> Unit
    ) {
        onLoading()
        val listener = newsCollection.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Gagal mengambil berita")
                    return@addSnapshotListener
                }
                val newsList = snapshot?.documents?.mapNotNull { it.toObject(News::class.java) } ?: emptyList()
                onSuccess(newsList)
            }
        listeners.add(listener)
    }

    /** Mengambil Berita Berdasarkan ID */
    fun fetchNewsById(
        newsId: String,
        onLoading: () -> Unit = {},
        onSuccess: (News) -> Unit,
        onError: (String) -> Unit
    ) {
        if (newsId.isBlank()) {
            onError("ID berita tidak boleh kosong")
            return
        }
        onLoading()
        val listener = newsCollection.document(newsId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Gagal mengambil berita")
                    return@addSnapshotListener
                }
                val news = snapshot?.toObject(News::class.java)
                if (news != null) {
                    onSuccess(news)
                } else {
                    onError("Berita tidak ditemukan")
                }
            }
        listeners.add(listener)
    }

    private fun generateSearchKeywords(title: String): List<String> {
        val keywords = title
            .lowercase()
            .split("\\s+".toRegex())
            .map { it.trim().replace("[^a-zA-Z0-9]".toRegex(), "") }
            .filter { it.isNotBlank() }

        // Generate semua prefiks untuk setiap kata
        val prefixes = mutableListOf<String>()
        keywords.forEach { word ->
            for (i in 1..word.length) {
                prefixes.add(word.substring(0, i))
            }
        }
        return prefixes.distinct()
    }

    fun addNews(
        news: News,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val keywords = generateSearchKeywords(news.title)
        val newsWithKeywords = news.copy(searchKeywords = keywords)
        val db = FirebaseFirestore.getInstance()
        db.collection("news").document(news.id)
            .set(newsWithKeywords)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Gagal menambah berita") }
    }
    fun toggleLike(
        newsId: String,
        userId: String,
        onSuccess: (Boolean) -> Unit, // Mengembalikan status apakah "liked" atau tidak
        onError: (String) -> Unit
    ) {
        val newsRef = newsCollection.document(newsId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(newsRef)
            val news = snapshot.toObject(News::class.java) ?: throw IllegalStateException("Berita tidak ditemukan")
            val currentLikes = news.likes.toMutableMap()
            val isLiked = currentLikes[userId] == true

            if (isLiked) {
                currentLikes.remove(userId) // Unlike
            } else {
                currentLikes[userId] = true // Like
            }

            transaction.update(newsRef, "likes", currentLikes)
            !isLiked // Mengembalikan status baru (true jika baru dilike, false jika unlike)
        }.addOnSuccessListener { isLiked ->
            onSuccess(isLiked)
        }.addOnFailureListener { e ->
            onError(e.message ?: "Gagal memperbarui like")
        }
    }

    fun fetchComments(
        newsId: String,
        onSuccess: (List<Comment>) -> Unit,
        onError: (String) -> Unit
    ) {
        val listener = newsCollection.document(newsId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Gagal mengambil komentar")
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents?.mapNotNull { it.toObject(Comment::class.java) } ?: emptyList()
                onSuccess(comments)
            }
        listeners.add(listener)
    }

    /** Menambah Komentar */
    fun addComment(
        newsId: String,
        text: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Pengguna belum login")
        if (text.isBlank()) return onError("Komentar tidak boleh kosong")

        val commentId = db.collection("temp").document().id // Generate ID unik
        val comment = Comment(
            id = commentId,
            userId = userId,
            text = text.trim(), // Hapus spasi berlebih
            createdAt = Timestamp.now()
        )

        val newsRef = newsCollection.document(newsId)
        val commentRef = newsRef.collection("comments").document(commentId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(newsRef)
            val news = snapshot.toObject(News::class.java) ?: throw IllegalStateException("Berita tidak ditemukan")

            // Tambah komentar ke subkoleksi
            transaction.set(commentRef, comment)
            // Tambah jumlah komentar di dokumen utama
            transaction.update(newsRef, "commentCount", news.commentCount + 1)
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { e ->
            onError(e.message ?: "Gagal menambah komentar")
        }
    }

    // Fungsi untuk mengambil berita yang di-bookmark
    fun fetchBookmarkedNews(
        onLoading: () -> Unit = {},
        onSuccess: (List<News>) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Pengguna belum login")
        onLoading()
        val listener = usersCollection.document(userId)
            .collection("bookmarks")
            .addSnapshotListener { bookmarkSnapshot, bookmarkError ->
                if (bookmarkError != null) {
                    onError(bookmarkError.message ?: "Gagal mengambil bookmark")
                    return@addSnapshotListener
                }
                val bookmarkIds = bookmarkSnapshot?.documents?.map { it.getString("newsId") ?: "" } ?: emptyList()
                if (bookmarkIds.isEmpty()) {
                    onSuccess(emptyList())
                    return@addSnapshotListener
                }

                newsCollection
                    .whereIn("id", bookmarkIds)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { newsSnapshot ->
                        val bookmarkedNews = newsSnapshot.documents.mapNotNull { it.toObject(News::class.java) }
                        onSuccess(bookmarkedNews)
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Gagal mengambil berita yang di-bookmark")
                    }
            }
        listeners.add(listener)
    }

    fun toggleBookmark(
        newsId: String,
        onSuccess: (Boolean) -> Unit, // Mengembalikan status apakah bookmarked atau tidak
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Pengguna belum login")
        val bookmarkRef = usersCollection.document(userId).collection("bookmarks").document(newsId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(bookmarkRef)
            val isBookmarked = snapshot.exists()

            if (isBookmarked) {
                transaction.delete(bookmarkRef) // Hapus bookmark
            } else {
                val bookmark = Bookmark(
                    newsId = newsId,
                    createdAt = Timestamp.now()
                )
                transaction.set(bookmarkRef, bookmark) // Tambah bookmark
            }
            !isBookmarked // Mengembalikan status baru
        }.addOnSuccessListener { isBookmarked ->
            onSuccess(isBookmarked)
        }.addOnFailureListener { e ->
            onError(e.message ?: "Gagal memperbarui bookmark")
        }
    }

    // Fungsi untuk memeriksa status bookmark
    fun checkBookmarkStatus(
        newsId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Pengguna belum login")
        val bookmarkRef = usersCollection.document(userId).collection("bookmarks").document(newsId)

        bookmarkRef.get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.exists())
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Gagal memeriksa bookmark")
            }
    }

    override fun onCleared() {
        // Membersihkan pendengar untuk mencegah kebocoran memori
        listeners.forEach { it.remove() }
        listeners.clear()
        super.onCleared()
    }
}