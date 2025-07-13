package com.myskripsi.gokos.data

import com.google.firebase.firestore.FirebaseFirestore
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.data.model.Campus
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class KosRepository(
    private val db: FirebaseFirestore
) {
    fun getCampusById(campusId: String): Flow<Result<Campus>> = flow {
        emit(Result.Loading)
        try {
            val snapshot = db.collection("kampus").document(campusId)
                .get()
                .await()

            val campus = snapshot.toObject(Campus::class.java)?.copy(id = snapshot.id)
            if (campus != null) {
                emit(Result.Success(campus))
            } else {
                emit(Result.Error("Campus not found!"))
            }
        } catch (e: Exception) {
            emit(Result.Error("Failed to retrieve campus data: ${e.message}"))
        }
    }
    suspend fun getKosById(kosId: String): Result<Kos> {
        return try {
            val snapshot = db.collection("kost").document(kosId).get().await()
            val kos = snapshot.toObject(Kos::class.java)?.copy(id = snapshot.id)
            if (kos != null) {
                Result.Success(kos)
            } else {
                Result.Error("Data kos dengan ID $kosId tidak ditemukan.")
            }
        } catch (e: Exception) {
            Result.Error("Gagal mengambil data kos: ${e.message}")
        }
    }
    fun getKosByCampusId(campusId: String): Flow<Result<List<Kos>>> = flow {
        emit(Result.Loading)
        try {
            val snapshot = db.collection("kost")
                .whereEqualTo("kampus_terdekat", campusId)
                .get()
                .await()

            val kosList = snapshot.documents.mapNotNull { document ->
                val kos = document.toObject(Kos::class.java)
                kos?.copy(id = document.id)
            }
            emit(Result.Success(kosList))
        } catch (e: Exception) {
            emit(Result.Error("Failed to retrieve campus data: ${e.message}"))
        }
    }

    fun getAllKos(): Flow<Result<List<Kos>>> = flow {
        emit(Result.Loading)
        try {
            val snapshot = db.collection("kost")
                .get()
                .await()

            val kosList = snapshot.documents.mapNotNull { document ->
                val kos = document.toObject(Kos::class.java)
                kos?.copy(id = document.id)
            }
            emit(Result.Success(kosList))
        } catch (e: Exception) {
            emit(Result.Error("Failed to retrieve campus data: ${e.message}"))
        }
    }

    fun getAllCampuses(): Flow<Result<List<Campus>>> = flow {
        emit(Result.Loading)
        try {
            val snapshot = db.collection("kampus")
                .get()
                .await()

            val campusList = snapshot.documents.mapNotNull { document ->
                val campus = document.toObject(Campus::class.java)
                campus?.copy(id = document.id)
            }
            emit(Result.Success(campusList))
        } catch (e: Exception) {
            emit(Result.Error("Failed to retrieve campus data: ${e.message}"))
        }
    }

}