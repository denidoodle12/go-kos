// PersonalDataViewModel.kt
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

// ViewModel ini hampir sama persis dengan EditProfileViewModel Anda yang lama
class PersonalDataViewModel(private val repository: UserProfileRepository, private val firebaseAuth: FirebaseAuth) : ViewModel() {
    private val _userProfileState = MutableLiveData<Result<UserProfile?>>()
    val userProfileState: LiveData<Result<UserProfile?>> = _userProfileState

    private val _saveProfileResult = MutableLiveData<Result<Unit>>()
    val saveProfileResult: LiveData<Result<Unit>> = _saveProfileResult

    private var currentLoadedProfile: UserProfile? = null

    fun loadUserProfile() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            _userProfileState.value = Result.Loading
            viewModelScope.launch {
                repository.getUserProfile(userId).collectLatest { result ->
                    if (result is Result.Success) {
                        currentLoadedProfile = result.data
                    }
                    // Tetap teruskan hasilnya ke UI
                    _userProfileState.value = result
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
        if (userId == null || currentLoadedProfile == null) {
            _saveProfileResult.value = Result.Error("Tidak bisa menyimpan, data profil awal tidak ditemukan.")
            return
        }

        // Buat objek baru dengan menyalin data yang ada dan hanya mengubah data pribadi
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