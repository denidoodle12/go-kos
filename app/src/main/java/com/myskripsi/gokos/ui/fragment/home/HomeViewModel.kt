package com.myskripsi.gokos.ui.fragment.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Kos
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: KosRepository) : ViewModel() {

}