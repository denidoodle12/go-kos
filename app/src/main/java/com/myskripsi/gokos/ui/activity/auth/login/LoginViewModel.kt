package com.myskripsi.gokos.ui.activity.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.myskripsi.gokos.data.AuthRepository
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository): ViewModel() {
    private val _loginState = MutableLiveData<Result<FirebaseUser>>()
    val loginState: LiveData<Result<FirebaseUser>> = _loginState

    private val _logoutState = MutableLiveData<Result<Unit>>()
    val logoutState: LiveData<Result<Unit>> = _logoutState

    fun loginUser(email: String, pass: String) {
        viewModelScope.launch {
            repository.loginUser(email, pass).collectLatest { result ->
                _loginState.value = result
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return repository.getCurrentUser()
    }

    fun logoutUser() {
        viewModelScope.launch {
            repository.logoutUser().collectLatest { result ->
                _logoutState.value = result
            }
        }
    }
}