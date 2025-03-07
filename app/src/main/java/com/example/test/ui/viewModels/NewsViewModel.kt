package com.example.test.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
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
import javax.inject.Inject

class NewsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val newsCollection = db.collection("News")
    private val bookmarksCollection = db.collection("Bookmarks")
    private val listeners = mutableListOf<ListenerRegistration>()

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

    /** Menambah Berita Baru */
    fun addNews(
        news: News,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (news.title.isBlank()) {
            onError("Judul berita tidak boleh kosong")
            return
        }
        val newsId = newsCollection.document().id
        val newNews = news.copy(
            id = newsId,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        newsCollection.document(newsId).set(newNews)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Gagal menambah berita") }
    }

    /** Menyukai atau Membatalkan Suka Berita */
    fun toggleLike(
        newsId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Pengguna belum login")

        db.runTransaction { transaction ->
            val newsRef = newsCollection.document(newsId)
            val snapshot = transaction.get(newsRef)
            if (!snapshot.exists()) throw IllegalStateException("Berita tidak ditemukan")

            val likes = snapshot.get("likes") as? Map<String, Boolean> ?: emptyMap()
            val isLiked = likes.containsKey(userId)
            if (isLiked) {
                transaction.update(newsRef, "likes.$userId", FieldValue.delete())
            } else {
                transaction.update(newsRef, "likes.$userId", true)
            }
            isLiked
        }.addOnSuccessListener { wasLiked -> onSuccess(!wasLiked) }
            .addOnFailureListener { e -> onError(e.message ?: "Gagal mengubah status suka") }
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

        val comment = Comment(
            id = "comment_${System.currentTimeMillis()}",
            userId = userId,
            text = text,
            createdAt = Timestamp.now()
        )

        db.runTransaction { transaction ->
            val newsRef = newsCollection.document(newsId)
            val snapshot = transaction.get(newsRef)
            val news = snapshot.toObject(News::class.java) ?: throw IllegalStateException("Berita tidak ditemukan")
            val updatedComments = news.comments + comment.id
            transaction.update(newsRef, "comments", updatedComments)
        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Gagal menambah komentar") }
    }

    /** Mengambil Bookmark */
    fun fetchBookmarks(
        onLoading: () -> Unit = {},
        onSuccess: (List<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Pengguna belum login")
        onLoading()

        val listener = bookmarksCollection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Gagal mengambil bookmark")
                    return@addSnapshotListener
                }
                val bookmarks = snapshot?.documents?.mapNotNull { it.getString("newsId") } ?: emptyList()
                onSuccess(bookmarks)
            }
        listeners.add(listener)
    }

    /** Menambah atau Menghapus Bookmark */
    fun toggleBookmark(
        newsId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Pengguna belum login")
        if (newsId.isBlank()) return onError("ID berita tidak boleh kosong")

        val bookmarkRef = bookmarksCollection.document("$userId-$newsId")

        bookmarkRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    bookmarkRef.delete()
                        .addOnSuccessListener { onSuccess(false) }
                        .addOnFailureListener { e -> onError(e.message ?: "Gagal menghapus bookmark") }
                } else {
                    val bookmark = Bookmark(userId, newsId, Timestamp.now())
                    bookmarkRef.set(bookmark)
                        .addOnSuccessListener { onSuccess(true) }
                        .addOnFailureListener { e -> onError(e.message ?: "Gagal menambah bookmark") }
                }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Gagal memeriksa bookmark") }
    }

    override fun onCleared() {
        // Membersihkan pendengar untuk mencegah kebocoran memori
        listeners.forEach { it.remove() }
        listeners.clear()
        super.onCleared()
    }
}