package com.myskripsi.gokos.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.data.model.Campus
import com.myskripsi.gokos.data.model.Lokasi
import com.myskripsi.gokos.utils.HaversineHelper
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class KosRepository(
    private val db: FirebaseFirestore
) {
    // Fungsi untuk mengambil detail kampus berdasarkan ID
    fun getCampusById(campusId: String): Flow<Result<Campus>> = flow {
        emit(Result.Loading)
        try {
            val snapshot = db.collection("kampus").document(campusId).get().await()
            val campus = snapshot.toObject(Campus::class.java)?.copy(id = snapshot.id)
            if (campus != null) {
                emit(Result.Success(campus))
            } else {
                emit(Result.Error("Kampus tidak ditemukan"))
            }
        } catch (e: Exception) {
            emit(Result.Error("Gagal mengambil data kampus: ${e.message}"))
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
            emit(Result.Error("Gagal mengambil data kost: ${e.message}"))
        }
    }

//    fun getNearbyKos(userLat: Double, userLng: Double): Flow<Result<List<Kos>>> = flow {
//        emit(Result.Loading)
//        try {
//            val campusSnapshot = db.collection("Kampus").get().await()
//            val campusList = campusSnapshot.documents.mapNotNull { document ->
//                document.toObject(Campus::class.java)?.copy(id = document.id)
//            }
//            val campusMap = campusList.associate { it.id to it }
//            Log.d("KosRepository", "Found ${campusList.size} campuses: ${campusList.map { it.nama_kampus }}")
//
//            val kosSnapshot = db.collection("kost").get().await()
//            val kosList = kosSnapshot.documents.mapNotNull { document ->
//                val kos = document.toObject(Kos::class.java)
//                kos?.let {
//                    val distanceToUser = HaversineHelper.calculateDistance(
//                        userLat, userLng, it.lokasi.latitude, it.lokasi.longitude
//                    )
//                    val campus = campusMap[it.kampus_terdekat]
//                    if (campus != null) {
//                        val distanceToCampus = if (it.lokasi.jarak > 0) {
//                            it.lokasi.jarak
//                        } else {
//                            HaversineHelper.calculateDistance(
//                                campus.lokasi.latitude, campus.lokasi.longitude,
//                                it.lokasi.latitude, it.lokasi.longitude
//                            )
//                        }
//                        Log.d("KosRepository", "Kos ${kos.nama_kost}: Distance to campus ${campus.nama_kampus} = $distanceToCampus km, Radius = ${campus.radius / 1000.0} km")
//
//                        if (distanceToCampus <= campus.radius / 1000.0) {
//                            it.copy(
//                                id = document.id,
//                                lokasi = it.lokasi.copy(jarak = distanceToCampus),
//                                nama_kampus = campus.nama_kampus
//                            )
//                        } else {
//                            Log.d("KosRepository", "Kos ${kos.nama_kost} filtered out: Distance $distanceToCampus km exceeds radius ${campus.radius / 1000.0} km")
//                            null
//                        }
//                    } else {
//                        Log.w("KosRepository", "No campus found for kampus_terdekat: ${it.kampus_terdekat}")
//                        null
//                    }
//                }
//            }.filterNotNull()
//
//            Log.d("KosRepository", "Total nearby kos: ${kosList.size}")
//            val sortedList = kosList.sortedBy { it.lokasi.jarak }
//            emit(Result.Success(sortedList))
//        } catch (e: Exception) {
//            Log.e("KosRepository", "Error in getNearbyKos: ${e.message}")
//            emit(Result.Error("Gagal mengambil data kost terdekat: ${e.message}"))
//        }
//    }

//    suspend fun updateAllKosJarak(): Result<Unit> {
//        return try {
//            val campusSnapshot = db.collection("Kampus").get().await()
//            val campusMap = campusSnapshot.documents.associate { doc ->
//                val campus = doc.toObject(Campus::class.java)
//                doc.id to campus
//            }
//            Log.d("KosRepository", "Loaded ${campusMap.size} campuses for jarak update")
//
//            val kosSnapshot = db.collection("kost").get().await()
//            val batch = db.batch()
//
//            kosSnapshot.documents.forEach { document ->
//                val kos = document.toObject(Kos::class.java)
//                kos?.let {
//                    val kampusId = it.kampus_terdekat
//                    val campus = campusMap[kampusId]
//                    if (campus != null && campus.lokasi.latitude != 0.0 && campus.lokasi.longitude != 0.0) {
//                        val distance = HaversineHelper.calculateDistance(
//                            it.lokasi.latitude, it.lokasi.longitude,
//                            campus.lokasi.latitude, campus.lokasi.longitude
//                        )
//                        val roundedDistance = (distance * 100).toInt() / 100.0
//                        batch.update(document.reference, mapOf("lokasi.jarak" to roundedDistance))
//                        Log.d("KosRepository", "Updating ${kos.nama_kost}: jarak = $roundedDistance km")
//                    } else {
//                        Log.w("KosRepository", "Invalid campus data for kampus_terdekat: $kampusId")
//                    }
//                }
//            }
//
//            batch.commit().await()
//            Log.d("KosRepository", "All kos jarak updated successfully")
//            Result.Success(Unit)
//        } catch (e: Exception) {
//            Log.e("KosRepository", "Error updating kos jarak: ${e.message}")
//            Result.Error("Gagal memperbarui jarak kost: ${e.message}")
//        }
//    }
}