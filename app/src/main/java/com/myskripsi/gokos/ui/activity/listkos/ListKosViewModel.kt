package com.myskripsi.gokos.ui.activity.listkos

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.ui.adapter.KosLayoutType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import com.myskripsi.gokos.utils.Result
import com.myskripsi.gokos.utils.HaversineHelper

class ListKosViewModel(private val repository: KosRepository) : ViewModel() {
    private val _kosState = MutableLiveData<Result<List<Kos>>>()
    val kosState: LiveData<Result<List<Kos>>> = _kosState

    private val _campusName = MutableLiveData<String>()
    val campusName: LiveData<String> = _campusName

    fun loadKosAndCampusDetails(campusId: String) {
        viewModelScope.launch {
            _kosState.value = Result.Loading

            val campusFlow = repository.getCampusById(campusId)
            val kosListFlow = repository.getKosByCampusId(campusId)

            campusFlow.zip(kosListFlow) { campusResult, kosResult ->
                Pair(campusResult, kosResult)
            }.collectLatest { (campusResult, kosResult) ->
                when {
                    campusResult is Result.Success && kosResult is Result.Success -> {
                        val campus = campusResult.data
                        val kosList = kosResult.data

                        _campusName.value = campus.nama_kampus

                        val kosListWithDistance = kosList.map { kos ->

                            if (kos.nama_kost.contains("Mamah Ica")) {
                                Log.d("HAVERSINE_DEBUG", "Menghitung Kost Mamah Ica:")
                                Log.d("HAVERSINE_DEBUG", "Kampus Lat: ${campus.lokasi.latitude}, Lon: ${campus.lokasi.longitude}")
                                Log.d("HAVERSINE_DEBUG", "Kos Lat: ${kos.lokasi.latitude}, Lon: ${kos.lokasi.longitude}")
                            }

                            val distance = HaversineHelper.calculateDistance(
                                campus.lokasi.latitude,
                                campus.lokasi.longitude,
                                kos.lokasi.latitude,
                                kos.lokasi.longitude
                            )

                            if (kos.nama_kost.contains("Mamah Ica")) {
                                Log.d("HAVERSINE_DEBUG", "Hasil Jarak (km): $distance")
                            }

                            kos.copy(lokasi = kos.lokasi.copy(jarak = distance),
                                layoutType = KosLayoutType.REGULAR)
                        }
                        _kosState.value = Result.Success(kosListWithDistance.sortedBy { it.lokasi.jarak })
                    }
                    campusResult is Result.Error -> {
                        _kosState.value = Result.Error(campusResult.message)
                    }
                    kosResult is Result.Error -> {
                        _kosState.value = Result.Error(kosResult.message)
                    }
                    campusResult is Result.Loading || kosResult is Result.Loading -> {
                        _kosState.value = Result.Loading
                    }
                }
            }
        }
    }
}