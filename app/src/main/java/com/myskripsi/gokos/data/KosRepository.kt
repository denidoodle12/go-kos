package com.myskripsi.gokos.data

import com.google.firebase.firestore.FirebaseFirestore
import com.myskripsi.gokos.data.model.Kos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class KosRepository(
    private val db: FirebaseFirestore
) {
    fun getKosByCampusId(campusId: String): Flow<Result<List<Kos>>> = flow {
        emit(Result.Loading)
        try {
            // Ambil data kost berdasarkan kampus terdekat
            val snapshot = db.collection("kost")
                .whereEqualTo("kampus_terdekat", campusId)
                .get()
                .await()

            // Transformasi data ke objek Kos
            val kosList = snapshot.documents.mapNotNull { document ->
                // Mengambil data dari Firestore dan mengkonversi ke objek Kos
                val kos = document.toObject(Kos::class.java)
                // Tambahkan ID dokumen ke objek Kos jika tidak null
                kos?.copy(id = document.id)
            }

            emit(Result.Success(kosList))
        } catch (e: Exception) {
            emit(Result.Error("Gagal mengambil data kost: ${e.message}"))
        }
    }

}