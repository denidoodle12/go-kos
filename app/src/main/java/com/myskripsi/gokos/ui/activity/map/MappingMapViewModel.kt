package com.myskripsi.gokos.ui.activity.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Campus
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.utils.HaversineHelper
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

            campusFlow.combine(kosListFlow) { campusResult, kosResult ->
                if (campusResult is Result.Success && kosResult is Result.Success) {
                    Result.Success(MapData(campusResult.data, kosResult.data))
                    val campus = campusResult.data
                    val originalKosList = kosResult.data
                    val kosListWithDistance = originalKosList.map { kos ->
                        val distance = HaversineHelper.calculateDistance(
                            campus.lokasi.latitude,
                            campus.lokasi.longitude,
                            kos.lokasi.latitude,
                            kos.lokasi.longitude
                        )
                        kos.copy(lokasi = kos.lokasi.copy(jarak = distance))
                        }
                    Result.Success(MapData(campus, kosListWithDistance))
                } else if (campusResult is Result.Error) {
                    Result.Error(campusResult.message)
                } else if (kosResult is Result.Error) {
                    Result.Error(kosResult.message)
                } else {
                    Result.Loading
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