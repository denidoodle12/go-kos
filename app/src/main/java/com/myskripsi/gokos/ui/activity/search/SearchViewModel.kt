// File: ui/activity/search/SearchViewModel.kt
package com.myskripsi.gokos.ui.activity.search

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Campus
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.ui.adapter.KosLayoutType
import com.myskripsi.gokos.utils.HaversineHelper
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: KosRepository) : ViewModel() {

    private var allKosList: List<Kos> = emptyList()

    private val _campusListState = MutableLiveData<Result<List<Campus>>>()
    val campusListState: LiveData<Result<List<Campus>>> = _campusListState

    private val _searchResultsState = MutableLiveData<List<Kos>>()
    val searchResultsState: LiveData<List<Kos>> = _searchResultsState

    private val _userLocation = MutableLiveData<Location?>()

    init {
        fetchInitialData()
    }
    fun updateUserLocation(location: Location?) {
        _userLocation.value = location
    }

    private fun fetchInitialData() {
        viewModelScope.launch {
            repository.getAllCampuses().collectLatest {
                _campusListState.value = it
            }
        }
        viewModelScope.launch {
            repository.getAllKos().collectLatest { result ->
                if (result is Result.Success) {
                    allKosList = result.data
                }
            }
        }
    }

    fun performSearch(query: String) {
        if (query.isBlank()) {
            _searchResultsState.value = emptyList()
            return
        }

        val currentUserLocation = _userLocation.value

        val filteredList = allKosList.filter { kos ->
            kos.nama_kost.contains(query, ignoreCase = true)
        }.map { kos ->
            val calculatedDistance = if (currentUserLocation != null) {
                HaversineHelper.calculateDistance(
                    currentUserLocation.latitude, currentUserLocation.longitude,
                    kos.lokasi.latitude, kos.lokasi.longitude
                )
            } else {
                -1.0
            }
            kos.copy(
                lokasi = kos.lokasi.copy(jarak = calculatedDistance),
                layoutType = KosLayoutType.REGULAR
            )
        }

        val sortedList = if (currentUserLocation != null) {
            filteredList.sortedBy { it.lokasi.jarak }
        } else {
            filteredList
        }
        _searchResultsState.value = sortedList
    }
}