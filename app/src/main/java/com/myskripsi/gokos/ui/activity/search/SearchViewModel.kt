// File: ui/activity/search/SearchViewModel.kt
package com.myskripsi.gokos.ui.activity.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Campus
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.ui.adapter.KosLayoutType
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: KosRepository) : ViewModel() {

    // Menyimpan daftar semua kos sekali saja untuk di-filter secara lokal
    private var allKosList: List<Kos> = emptyList()

    // LiveData untuk daftar kampus di tampilan awal
    private val _campusListState = MutableLiveData<Result<List<Campus>>>()
    val campusListState: LiveData<Result<List<Campus>>> = _campusListState

    // LiveData untuk hasil pencarian
    private val _searchResultsState = MutableLiveData<List<Kos>>()
    val searchResultsState: LiveData<List<Kos>> = _searchResultsState

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        viewModelScope.launch {
            // Ambil daftar kampus untuk ditampilkan di awal
            repository.getAllCampuses().collectLatest {
                _campusListState.value = it
            }
        }
        viewModelScope.launch {
            // Ambil SEMUA data kos dan simpan di memori untuk pencarian cepat
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

        // Filter dari daftar yang sudah ada di memori, bukan dari database
        val filteredList = allKosList.filter { kos ->
            kos.nama_kost.contains(query, ignoreCase = true)
        }.map {
            // Pastikan layout type-nya REGULAR untuk tampilan vertikal
            it.copy(layoutType = KosLayoutType.REGULAR)
        }
        _searchResultsState.value = filteredList
    }
}