package com.myskripsi.gokos.ui.fragment.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.myskripsi.gokos.data.AuthRepository
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _logoutState = MutableLiveData<Result<Unit>>()
    val logoutState: LiveData<Result<Unit>> = _logoutState

    private val _userProfile = MutableLiveData<FirebaseUser?>()
    val userProfile: LiveData<FirebaseUser?> = _userProfile

    fun loadUserProfile() {
        val currentUser = authRepository.getCurrentUser()
        _userProfile.value = currentUser
    }

    fun logoutUser() {
        viewModelScope.launch {
            authRepository.logoutUser().collectLatest { result ->
                _logoutState.value = result
            }
        }
    }
}