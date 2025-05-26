package com.myskripsi.gokos.ui.activity.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Campus
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

data class MapData(
    val campus: Campus,
    val kosList: List<Kos>
)

class MappingMapViewModel(private val repository: KosRepository) : ViewModel() {
    private val _mapDataState = MutableLiveData<Result<MapData>>()
    val mapDataState: LiveData<Result<MapData>> = _mapDataState

    fun loadCampusAndKosDetails(campusId: String) {
        viewModelScope.launch {
            val campusFlow = repository.getCampusById(campusId)
            val kosListFlow = repository.getKosByCampusId(campusId)

            // Menggabungkan kedua flow
            campusFlow.combine(kosListFlow) { campusResult, kosResult ->
                // Proses hasil hanya jika keduanya sukses
                if (campusResult is Result.Success && kosResult is Result.Success) {
                    Result.Success(MapData(campusResult.data, kosResult.data))
                } else if (campusResult is Result.Error) {
                    Result.Error(campusResult.message) // Prioritaskan error kampus jika ada
                } else if (kosResult is Result.Error) {
                    Result.Error(kosResult.message) // Atau error kos
                } else {
                    Result.Loading // Jika salah satu masih loading atau keduanya
                }
            }
                .onStart { _mapDataState.value = Result.Loading }
                .catch { e -> _mapDataState.value = Result.Error("Error combine flow: ${e.message}") }
                .collectLatest { combinedResult ->
                    _mapDataState.value = combinedResult
                }
        }
    }
}