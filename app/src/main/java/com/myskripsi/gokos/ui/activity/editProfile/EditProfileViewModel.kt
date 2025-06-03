package com.myskripsi.gokos.ui.activity.editProfile

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

class EditProfileViewModel(private val repository: UserProfileRepository, private val firebaseAuth: FirebaseAuth) : ViewModel() {
    private val _userProfileState = MutableLiveData<Result<UserProfile?>>()
    val userProfileState: LiveData<Result<UserProfile?>> = _userProfileState

    private val _saveProfileResult = MutableLiveData<Result<Unit>>()
    val saveProfileResult: LiveData<Result<Unit>> = _saveProfileResult

    private val _uploadImageResult = MutableLiveData<Result<String>>()
    val uploadImageResult: LiveData<Result<String>> = _uploadImageResult

    private var currentLoadedProfile: UserProfile? = null

    fun loadUserProfile() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            _userProfileState.value = Result.Loading
            viewModelScope.launch {
                repository.getUserProfile(userId).collectLatest { result ->
                    _userProfileState.value = result
                    if (result is Result.Success) {
                        currentLoadedProfile = result.data ?: UserProfile(
                            uid = userId,
                            email = firebaseAuth.currentUser?.email ?: ""
                        )
                    } else if (result is Result.Error && currentLoadedProfile == null) {
                        currentLoadedProfile = UserProfile(
                            uid = userId,
                            email = firebaseAuth.currentUser?.email ?: ""
                        )
                        _userProfileState.value = Result.Success(currentLoadedProfile)
                    }
                }
            }
        } else {
            _userProfileState.value = Result.Error("Pengguna tidak diautentikasi.")
        }
    }

    fun saveUserProfile(
        fullName: String,
        gender: String?,
        dateOfBirth: String?,
        profession: String?,
        professionName: String?,
        maritalStatus: String?,
        emergencyContact: String?
    ) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            _saveProfileResult.value = Result.Error("Pengguna tidak valid untuk menyimpan profil.")
            return
        }

        val profileToSave = currentLoadedProfile?.copy(
            fullName = fullName,
            gender = gender,
            dateOfBirth = dateOfBirth,
            profession = profession,
            professionName = professionName,
            maritalStatus = maritalStatus,
            emergencyContact = emergencyContact
        ) ?: UserProfile(
            uid = userId,
            email = firebaseAuth.currentUser?.email ?: "",
            fullName = fullName,
            gender = gender,
            dateOfBirth = dateOfBirth,
            profession = profession,
            professionName = professionName,
            maritalStatus = maritalStatus,
            emergencyContact = emergencyContact,
        )

        viewModelScope.launch {
            repository.saveUserProfile(userId, profileToSave).collectLatest {
                _saveProfileResult.value = it
            }
        }
    }

}