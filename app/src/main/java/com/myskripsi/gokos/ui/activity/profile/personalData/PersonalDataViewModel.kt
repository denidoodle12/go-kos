// File: ui/activity/profile/personalData/PersonalDataViewModel.kt
package com.myskripsi.gokos.ui.activity.profile.personalData

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.myskripsi.gokos.data.UserProfileRepository
import com.myskripsi.gokos.data.model.UserProfile
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PersonalDataViewModel(private val repository: UserProfileRepository, private val firebaseAuth: FirebaseAuth) : ViewModel() {
    private val _userProfileState = MutableLiveData<Result<UserProfile?>>()
    val userProfileState: LiveData<Result<UserProfile?>> = _userProfileState

    private val _saveProfileResult = MutableLiveData<Result<Unit>>()
    val saveProfileResult: LiveData<Result<Unit>> = _saveProfileResult

    private var currentLoadedProfile: UserProfile? = null

    fun loadUserProfile() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser?.uid != null) {
            _userProfileState.value = Result.Loading
            viewModelScope.launch {
                repository.getUserProfile(firebaseUser.uid).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            // Jika profil ditemukan di Firestore, gunakan itu.
                            currentLoadedProfile = result.data
                            _userProfileState.value = result
                        }
                        is Result.Error -> {
                            // --- INI LOGIKA PENTING YANG DISALIN ---
                            // Jika profil TIDAK DITEMUKAN (kasus pengguna baru),
                            // buat profil default dari data Firebase Auth.
                            val defaultProfile = UserProfile(
                                uid = firebaseUser.uid,
                                fullName = firebaseUser.displayName ?: "",
                                email = firebaseUser.email ?: ""
                            )
                            currentLoadedProfile = defaultProfile
                            _userProfileState.value = Result.Success(defaultProfile)
                        }
                        is Result.Loading -> { /* Do nothing */ }
                    }
                }
            }
        } else {
            _userProfileState.value = Result.Error("Pengguna tidak diautentikasi.")
        }
    }

    fun savePersonalData(
        gender: String?,
        dateOfBirth: String?,
        profession: String?,
        professionName: String?,
        maritalStatus: String?,
        emergencyContact: String?
    ) {
        val userId = firebaseAuth.currentUser?.uid
        // Pemeriksaan ini sekarang akan berhasil karena 'currentLoadedProfile' tidak akan pernah null
        if (userId == null || currentLoadedProfile == null) {
            _saveProfileResult.value = Result.Error("Tidak bisa menyimpan, data profil awal tidak ditemukan.")
            return
        }

        val profileToSave = currentLoadedProfile!!.copy(
            gender = gender,
            dateOfBirth = dateOfBirth,
            profession = profession,
            professionName = professionName,
            maritalStatus = maritalStatus,
            emergencyContact = emergencyContact
        )

        viewModelScope.launch {
            repository.saveUserProfile(userId, profileToSave).collectLatest {
                _saveProfileResult.value = it
            }
        }
    }
}