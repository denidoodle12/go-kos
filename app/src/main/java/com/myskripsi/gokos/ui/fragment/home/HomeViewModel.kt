package com.myskripsi.gokos.ui.fragment.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Kos
import android.location.Location
import com.myskripsi.gokos.data.AuthRepository
import com.myskripsi.gokos.data.model.Campus
import com.myskripsi.gokos.ui.adapter.KosLayoutType
import com.myskripsi.gokos.utils.HaversineHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.myskripsi.gokos.utils.Result

enum class PriceRangeFilter {
    BELOW_500K,
    BETWEEN_500K_700K,
    ABOVE_700K
}

class HomeViewModel(private val repository: KosRepository, private val authRepository: AuthRepository) : ViewModel() {
    private val _nearbyKosState = MutableLiveData<Result<List<Kos>>>()
    val nearbyKosState: LiveData<Result<List<Kos>>> = _nearbyKosState

    private val _campusListState = MutableLiveData<Result<List<Campus>>>()
    val campusListState: LiveData<Result<List<Campus>>> = _campusListState

    private val _userLocation = MutableLiveData<Location?>()
    val userLocation: LiveData<Location?> = _userLocation

    private val _userDisplayName = MutableLiveData<String?>()
    val userDisplayName: LiveData<String?> = _userDisplayName

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> = _userEmail

    private val _allKosList = MutableLiveData<List<Kos>>()

    private val _budgetKosState = MutableLiveData<Result<List<Kos>>>()
    val budgetKosState: LiveData<Result<List<Kos>>> = _budgetKosState

    private val _selectedPriceRange = MutableLiveData<PriceRangeFilter>()
    val selectedPriceRange: LiveData<PriceRangeFilter> = _selectedPriceRange


    fun loadUserProfile() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _userDisplayName.value = currentUser.displayName
            _userEmail.value = currentUser.email
        } else {
            _userDisplayName.value = null
            _userEmail.value = null
        }
    }

    fun updateUserLocation(location: Location?) {
        _userLocation.value = location
        if (location != null) {
            fetchNearbyKos(location.latitude, location.longitude)
        } else {
            _nearbyKosState.value = Result.Error("User location not found to search for nearby kos!.")
        }
    }

    fun fetchCampusList() {
        viewModelScope.launch {
            repository.getAllCampuses().collectLatest { result ->
                _campusListState.value = result
            }
        }
    }

    private fun fetchNearbyKos(userLat: Double, userLon: Double) {
        viewModelScope.launch {
            _nearbyKosState.value = Result.Loading
            repository.getAllKos().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val allKosListFromRepo = result.data
                        _allKosList.value = allKosListFromRepo

                        if (allKosListFromRepo.isEmpty()) {
                            _nearbyKosState.value = Result.Success(emptyList())
                            if(_selectedPriceRange.value != null && _budgetKosState.value == null){
                                filterKosByBudget(_selectedPriceRange.value!!)
                            } else if (_budgetKosState.value == null) {
                                filterKosByBudget(PriceRangeFilter.BELOW_500K)
                            }
                            return@collectLatest
                        }

                        val kosWithDistance = allKosListFromRepo.map { kos ->
                            val distance = HaversineHelper.calculateDistance(
                                userLat,
                                userLon,
                                kos.lokasi.latitude,
                                kos.lokasi.longitude
                            )
                            kos.copy(
                                lokasi = kos.lokasi.copy(jarak = distance),
                                layoutType = KosLayoutType.NEARBY
                            )
                        }

                        val sortedNearbyKos = kosWithDistance.sortedBy { it.lokasi.jarak }
                        _nearbyKosState.value = Result.Success(sortedNearbyKos)

                        if(_selectedPriceRange.value != null){
                            filterKosByBudget(_selectedPriceRange.value!!)
                        } else {
                            filterKosByBudget(PriceRangeFilter.BELOW_500K)
                        }
                    }
                    is Result.Error -> {
                        _nearbyKosState.value = Result.Error(result.message)
                        _budgetKosState.value = Result.Error("Failed to load base data for budget filtering: ${result.message}")
                    }
                    is Result.Loading -> {
                        _nearbyKosState.value = Result.Loading
                        if (_allKosList.value.isNullOrEmpty()) {
                            _budgetKosState.value = Result.Loading
                        }
                    }
                }
            }
        }
    }

    fun filterKosByBudget(priceRange: PriceRangeFilter) {
        _selectedPriceRange.value = priceRange
        _budgetKosState.value = Result.Loading

        viewModelScope.launch {
            val currentAllKos = _allKosList.value
            val currentUserLocation = _userLocation.value

            if (currentAllKos.isNullOrEmpty()) {
                if (nearbyKosState.value is Result.Success || nearbyKosState.value is Result.Error) {
                    _budgetKosState.value = Result.Success(emptyList())
                }
                return@launch
            }

            val filteredList = currentAllKos.filter { kos ->
                when (priceRange) {
                    PriceRangeFilter.BELOW_500K -> kos.harga < 500000
                    PriceRangeFilter.BETWEEN_500K_700K -> kos.harga >= 500000 && kos.harga <= 700000
                    PriceRangeFilter.ABOVE_700K -> kos.harga > 700000
                }
            }.map { kos ->
                var kosOutput = kos

                currentUserLocation?.let { location ->
                    if (kos.lokasi.latitude != 0.0 || kos.lokasi.longitude != 0.0) {
                        val distance = HaversineHelper.calculateDistance(
                            location.latitude,
                            location.longitude,
                            kos.lokasi.latitude,
                            kos.lokasi.longitude
                        )
                        kosOutput = kosOutput.copy(lokasi = kosOutput.lokasi.copy(jarak = distance))
                    } else {
                        kosOutput = kosOutput.copy(lokasi = kosOutput.lokasi.copy(jarak = -1.0))
                    }
                }

                kosOutput.copy(layoutType = KosLayoutType.NEARBY)
            }

            _budgetKosState.value = Result.Success(filteredList)
        }
    }

}