package com.myskripsi.gokos.ui.activity.auth.signup

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.myskripsi.gokos.data.AuthRepository
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SignupViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _signupState = MutableLiveData<Result<FirebaseUser>>()
    val signupState: LiveData<Result<FirebaseUser>> = _signupState

    fun signupUser(fullName: String, email: String, pass: String, confirmPass: String) {
        if (fullName.isBlank() || email.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            _signupState.value = Result.Error("All fields must be filled in.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _signupState.value = Result.Error("Invalid email format.")
            return
        }

        if (pass != confirmPass) {
            _signupState.value = Result.Error("Password and password confirmation do not match")
            return
        }
        if (pass.length < 6) {
            _signupState.value = Result.Error("The password must be at least 6 characters long.")
            return
        }

        viewModelScope.launch {
            repository.registerUser(fullName, email, pass).collectLatest { result ->
                _signupState.value = result
            }
        }
    }
}