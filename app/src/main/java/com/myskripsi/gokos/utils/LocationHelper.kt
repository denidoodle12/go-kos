package com.myskripsi.gokos.utils // Atau package lain yang sesuai

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(
    private val fragment: Fragment
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(fragment.requireActivity()) // DIUBAH: Menggunakan fragment.requireActivity()

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var onPermissionResultCallback: ((Map<String, Boolean>) -> Unit)? = null

    init {
        // Inisialisasi launcher menggunakan Fragment, yang akan dipanggil di onCreate Fragment
        permissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            onPermissionResultCallback?.invoke(permissions)
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            fragment.requireContext(), // DIUBAH: Menggunakan fragment.requireContext()
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            fragment.requireContext(), // DIUBAH
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermissions(callback: (Map<String, Boolean>) -> Unit) {
        onPermissionResultCallback = callback
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult {
        if (!hasLocationPermission()) {
            return LocationResult.PermissionDenied
        }

        val locationManager = fragment.requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager // DIUBAH
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            return LocationResult.LocationDisabled
        }

        return try {
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                LocationResult.Success(lastLocation)
            } else {
                requestSingleLocationUpdate()
            }
        } catch (e: SecurityException) {
            LocationResult.PermissionDenied
        } catch (e: Exception) {
            LocationResult.Error("Gagal mendapatkan lokasi terakhir: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocationUpdate(): LocationResult = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            if (continuation.isActive) continuation.resume(LocationResult.PermissionDenied)
            return@suspendCancellableCoroutine
        }

        val locationManager = fragment.requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager // DIUBAH
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            if (continuation.isActive) continuation.resume(LocationResult.LocationDisabled)
            return@suspendCancellableCoroutine
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMaxUpdates(1)
            .build()

        var locationCallback: LocationCallback? = null
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResultCallback: com.google.android.gms.location.LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                locationResultCallback.lastLocation?.let {
                    if (continuation.isActive) continuation.resume(LocationResult.Success(it))
                } ?: run {
                    if (continuation.isActive) continuation.resume(LocationResult.Error("Tidak ada lokasi ditemukan dalam hasil update."))
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    fusedLocationClient.removeLocationUpdates(this)
                    if (continuation.isActive) continuation.resume(LocationResult.Error("Layanan lokasi tidak tersedia saat meminta update."))
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            if (continuation.isActive) continuation.resume(LocationResult.PermissionDenied)
        }

        continuation.invokeOnCancellation {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}

// Extension function Task<T>.await() tetap sama
suspend fun <T> Task<T>.await(): T? {
    if (isComplete) {
        return if (isSuccessful) result else {
            null
        }
    }
    return suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener {
            if (cont.isActive) cont.resume(null)
        }
        addOnCanceledListener {
            if (cont.isActive) cont.cancel()
        }
    }
}
