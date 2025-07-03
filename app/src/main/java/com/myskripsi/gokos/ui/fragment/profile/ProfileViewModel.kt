// File: ui/fragment/profile/ProfileViewModel.kt
package com.myskripsi.gokos.ui.fragment.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.myskripsi.gokos.data.AuthRepository
import com.myskripsi.gokos.data.UserProfileRepository
import com.myskripsi.gokos.data.model.UserProfile
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: UserProfileRepository, // <-- Tambahkan dependensi ini
    private val firebaseAuth: FirebaseAuth // <-- Tambahkan dependensi ini
) : ViewModel() {

    private val _logoutState = MutableLiveData<Result<Unit>>()
    val logoutState: LiveData<Result<Unit>> = _logoutState

    // Ubah LiveData untuk menampung UserProfile yang lebih lengkap
    private val _userProfileState = MutableLiveData<Result<UserProfile?>>()
    val userProfileState: LiveData<Result<UserProfile?>> = _userProfileState

    fun loadUserProfile() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser?.uid != null) {
            _userProfileState.value = Result.Loading
            viewModelScope.launch {
                profileRepository.getUserProfile(firebaseUser.uid).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            _userProfileState.value = result
                        }
                        is Result.Error -> {
                            // Jika dokumen profil belum ada di Firestore (pengguna baru),
                            // buat objek default dari Firebase Auth agar UI tidak error.
                            val defaultProfile = UserProfile(
                                uid = firebaseUser.uid,
                                fullName = firebaseUser.displayName ?: "Pengguna GoKos",
                                email = firebaseUser.email ?: "",
                                profileImageUrl = firebaseUser.photoUrl?.toString()
                            )
                            _userProfileState.value = Result.Success(defaultProfile)
                        }
                        is Result.Loading -> {}
                    }
                }
            }
        } else {
            _userProfileState.value = Result.Error("Pengguna tidak diautentikasi.")
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            authRepository.logoutUser().collectLatest { result ->
                _logoutState.value = result
            }
        }
    }
}