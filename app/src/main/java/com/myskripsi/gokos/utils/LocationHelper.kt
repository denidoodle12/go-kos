package com.myskripsi.gokos.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.tasks.await

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Menggunakan Priority class yang baru (tidak deprecated)
    private val locationRequest: LocationRequest by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdateDelayMillis(15000)
            .build()
    }

    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocationSuspend(): Location? {
        if (!hasLocationPermission()) {
            Log.e("LocationHelper", "Location permission not granted")
            return null
        }

        return try {
            // Explicitly check permissions again before accessing location services
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("LocationHelper", "Location permission not granted")
                return null
            }

            // Try to get last location first
            val lastLocation = fusedLocationClient.lastLocation.await()

            lastLocation ?: requestNewLocationSuspend()
        } catch (e: SecurityException) {
            Log.e("LocationHelper", "Security exception: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e("LocationHelper", "Error getting location: ${e.message}")
            null
        }
    }

    fun getCurrentLocation(callback: (Location?) -> Unit) {
        if (!hasLocationPermission()) {
            Log.e("LocationHelper", "Location permission not granted")
            callback(null)
            return
        }

        try {
            // Explicitly check permissions again before accessing location services
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("LocationHelper", "Location permission not granted")
                callback(null)
                return
            }

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        callback(location)
                    } else {
                        // Last location not available, request new location
                        requestNewLocation(callback)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LocationHelper", "Error getting location: ${e.message}")
                    callback(null)
                }
        } catch (e: SecurityException) {
            Log.e("LocationHelper", "Security exception: ${e.message}")
            callback(null)
        } catch (e: Exception) {
            Log.e("LocationHelper", "Exception in getCurrentLocation: ${e.message}")
            callback(null)
        }
    }

    private suspend fun requestNewLocationSuspend(): Location? {
        return try {
            // Explicitly check permissions before accessing location services
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("LocationHelper", "Location permission not granted")
                return null
            }

            val result = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()
            result
        } catch (e: SecurityException) {
            Log.e("LocationHelper", "Security exception: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e("LocationHelper", "Error getting current location: ${e.message}")
            null
        }
    }

    private fun requestNewLocation(callback: (Location?) -> Unit) {
        if (!hasLocationPermission()) {
            Log.e("LocationHelper", "Location permission not granted")
            callback(null)
            return
        }

        try {
            // Explicitly check permissions before accessing location services
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("LocationHelper", "Location permission not granted")
                callback(null)
                return
            }

            // Using newer getCurrentLocation API (not deprecated)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    callback(location)
                }
                .addOnFailureListener { e ->
                    Log.e("LocationHelper", "Error getting current location: ${e.message}")
                    callback(null)
                }
        } catch (e: SecurityException) {
            Log.e("LocationHelper", "Security exception: ${e.message}")
            callback(null)
        } catch (e: Exception) {
            Log.e("LocationHelper", "Exception in requestNewLocation: ${e.message}")
            callback(null)
        }
    }

    // For periodic location updates
    fun requestLocationUpdates(callback: (Location) -> Unit) {
        if (!hasLocationPermission()) {
            Log.e("LocationHelper", "Location permission not granted")
            return
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { callback(it) }
            }
        }

        try {
            // Explicitly check permissions before accessing location services
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("LocationHelper", "Location permission not granted")
                return
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("LocationHelper", "Security exception: ${e.message}")
        } catch (e: Exception) {
            Log.e("LocationHelper", "Exception in requestLocationUpdates: ${e.message}")
        }
    }
}