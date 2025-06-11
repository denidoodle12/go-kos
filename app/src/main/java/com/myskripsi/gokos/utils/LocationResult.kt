package com.myskripsi.gokos.utils

import android.location.Location

sealed class LocationResult {
    data class Success(val location: Location) : LocationResult()
    data class Error(val message: String) : LocationResult()
    object PermissionDenied : LocationResult()
    object LocationDisabled : LocationResult()
    object Loading : LocationResult()
}