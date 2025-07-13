// File: data/FavoriteRepository.kt
package com.myskripsi.gokos.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.myskripsi.gokos.data.model.Favorite
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FavoriteRepository(private val db: FirebaseFirestore) {

    private val favoritesCollection = db.collection("favorites")

    // Mendapatkan semua data favorit untuk pengguna tertentu
    fun getFavorites(userId: String): Flow<Result<List<Favorite>>> = flow {
        emit(Result.Loading)
        try {
            val snapshot = favoritesCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val favoriteList = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Favorite::class.java)?.copy(id = doc.id)
            }
            emit(Result.Success(favoriteList))
        } catch (e: Exception) {
            emit(Result.Error("Gagal memuat data favorit: ${e.message}"))
        }
    }

    // Menambahkan kos ke favorit
    fun addFavorite(favorite: Favorite): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            favoritesCollection.add(favorite).await()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error("Gagal menyimpan ke favorit: ${e.message}"))
        }
    }

    fun updateNote(favoriteId: String, newNote: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val noteToSave = if (newNote.isBlank()) null else newNote
            favoritesCollection.document(favoriteId)
                .update("note", noteToSave)
                .await()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error("Gagal memperbarui catatan: ${e.message}"))
        }
    }

    // Menghapus kos dari favorit berdasarkan ID dokumen favorit
    fun removeFavorite(favoriteId: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            favoritesCollection.document(favoriteId).delete().await()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error("Gagal menghapus dari favorit: ${e.message}"))
        }
    }

    // Mengecek apakah sebuah kos sudah difavoritkan oleh user
    fun isFavorited(userId: String, kosId: String): Flow<Result<Favorite?>> = flow {
        emit(Result.Loading)
        try {
            val snapshot = favoritesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("kosId", kosId)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                emit(Result.Success(null)) // Tidak ada, belum favorit
            } else {
                val favorite = snapshot.documents[0].toObject(Favorite::class.java)?.copy(id = snapshot.documents[0].id)
                emit(Result.Success(favorite)) // Ada, sudah favorit
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Gagal cek status favorit"))
        }
    }
}