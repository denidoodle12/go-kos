package com.myskripsi.gokos.ui.fragment.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.AuthRepository
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _logoutState = MutableLiveData<Result<Unit>>()
    val logoutState: LiveData<Result<Unit>> = _logoutState

    // Opsional: Anda mungkin ingin memuat data pengguna di sini juga
    // val currentUser = authRepository.getCurrentUser()

    fun logoutUser() {
        viewModelScope.launch {
            authRepository.logoutUser().collectLatest { result ->
                _logoutState.value = result
            }
        }
    }
}