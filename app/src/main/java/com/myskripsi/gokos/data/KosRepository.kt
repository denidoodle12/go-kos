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

}