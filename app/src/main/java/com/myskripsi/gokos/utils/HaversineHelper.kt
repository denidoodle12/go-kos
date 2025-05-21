package com.myskripsi.gokos.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object HaversineHelper {
    private const val EARTH_RADIUS = 6371.0 // Radius bumi dalam kilometer

    /**
     * Menghitung jarak antara dua titik menggunakan formula Haversine
     * @param lat1 Latitude titik pertama dalam derajat
     * @param lon1 Longitude titik pertama dalam derajat
     * @param lat2 Latitude titik kedua dalam derajat
     * @param lon2 Longitude titik kedua dalam derajat
     * @return Jarak dalam kilometer
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)

        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS * c
    }
}