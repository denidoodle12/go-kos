package com.myskripsi.gokos.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Kos
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.myskripsi.gokos.data.Result

class ListKosViewModel(private val repository: KosRepository) : ViewModel() {
    private val _kosState = MutableLiveData<Result<List<Kos>>>()
    val kosState: LiveData<Result<List<Kos>>> = _kosState

    fun getKosByCampusId(campusId: String) {
        viewModelScope.launch {
            repository.getKosByCampusId(campusId).collectLatest { result ->
                _kosState.value = result
            }
        }
    }
}