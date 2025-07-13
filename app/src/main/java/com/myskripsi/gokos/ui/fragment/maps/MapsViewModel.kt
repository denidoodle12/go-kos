package com.myskripsi.gokos.ui.fragment.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Campus
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MapsViewModel(private val repository: KosRepository) : ViewModel() {

    private val _campusListState = MutableLiveData<Result<List<Campus>>>()
    val campusListState: LiveData<Result<List<Campus>>> = _campusListState

    fun fetchCampuses() {
        viewModelScope.launch {
            repository.getAllCampuses().collectLatest {
                _campusListState.value = it
            }
        }
    }
}