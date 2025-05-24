package com.myskripsi.gokos.ui.fragment.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Kos
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.myskripsi.gokos.utils.Result

class HomeViewModel(private val repository: KosRepository) : ViewModel() {
    private val _nearbyKos = MutableLiveData<Result<List<Kos>>>()
    val nearbyKos: LiveData<Result<List<Kos>>> = _nearbyKos

//    fun getNearbyKos(userLat: Double, userLng: Double) {
//        viewModelScope.launch {
//            repository.getNearbyKos(userLat, userLng).collectLatest { result ->
//                _nearbyKos.value = result
//            }
//        }
//    }
//
//    suspend fun updateAllKosJarak(): Result<Unit> {
//        return repository.updateAllKosJarak()
//    }
}