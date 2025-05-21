package com.myskripsi.gokos.data

import com.google.firebase.firestore.FirebaseFirestore
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.utils.HaversineHelper
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class KosRepository(
    private val db: FirebaseFirestore
) {
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
            emit(Result.Error("Gagal mengambil data kost: ${e.message}"))
        }
    }

    fun getNearbyKos(userLat: Double, userLng: Double): Flow<Result<List<Kos>>> = flow {
        emit(Result.Loading)
        try {
            val snapshot = db.collection("kost")
                .get()
                .await()

            val kosList = snapshot.documents.mapNotNull { document ->
                val kos = document.toObject(Kos::class.java)
                kos?.let {
                    val distance = HaversineHelper.calculateDistance(
                        userLat, userLng, it.lokasi.latitude, it.lokasi.longitude
                    )
                    it.copy(id = document.id, lokasi = it.lokasi.copy(jarak = distance))
                }
            }

            val sortedList = kosList.sortedBy { it.lokasi.jarak }
            emit(Result.Success(sortedList))
        } catch (e: Exception) {
            emit(Result.Error("Gagal mengambil data kost terdekat: ${e.message}"))
        }
    }
}