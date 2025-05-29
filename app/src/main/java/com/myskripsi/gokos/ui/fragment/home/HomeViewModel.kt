package com.myskripsi.gokos.ui.fragment.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Kos
import android.location.Location
import com.myskripsi.gokos.ui.adapter.KosLayoutType
import com.myskripsi.gokos.utils.HaversineHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.myskripsi.gokos.utils.Result

class HomeViewModel(private val repository: KosRepository) : ViewModel() {
    private val _nearbyKosState = MutableLiveData<Result<List<Kos>>>()
    val nearbyKosState: LiveData<Result<List<Kos>>> = _nearbyKosState

    private val _userLocation = MutableLiveData<Location?>()
    val userLocation: LiveData<Location?> = _userLocation

    fun updateUserLocation(location: Location?) {
        _userLocation.value = location
        if (location != null) {
            fetchNearbyKos(location.latitude, location.longitude)
        } else {
            // Jika lokasi null, mungkin tampilkan pesan error atau state tertentu
            _nearbyKosState.value = Result.Error("Lokasi pengguna tidak ditemukan untuk mencari kos terdekat.")
        }
    }

    private fun fetchNearbyKos(userLat: Double, userLon: Double) {
        viewModelScope.launch {
            _nearbyKosState.value = Result.Loading
            repository.getAllKos().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val allKosList = result.data
                        if (allKosList.isEmpty()) {
                            _nearbyKosState.value = Result.Success(emptyList()) // Tidak ada kos sama sekali
                            return@collectLatest
                        }

                        val kosWithDistance = allKosList.map { kos ->
                            val distance = HaversineHelper.calculateDistance(
                                userLat,
                                userLon,
                                kos.lokasi.latitude,
                                kos.lokasi.longitude
                            )
                            kos.copy(
                                lokasi = kos.lokasi.copy(jarak = distance),
                                layoutType = KosLayoutType.NEARBY)
                        }

                        val sortedNearbyKos = kosWithDistance.sortedBy { it.lokasi.jarak }
                        _nearbyKosState.value = Result.Success(sortedNearbyKos.take(5))
                    }
                    is Result.Error -> {
                        _nearbyKosState.value = Result.Error(result.message)
                    }
                    is Result.Loading -> {
                        _nearbyKosState.value = Result.Loading
                    }
                }
            }
        }
    }

}