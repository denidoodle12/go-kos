package com.myskripsi.gokos.ui.activity.profile.editProfile

import android.net.Uri
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

    private var currentLoadedProfile: UserProfile? = null

    fun loadUserProfile() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser?.uid != null) {
            _userProfileState.value = Result.Loading
            viewModelScope.launch {
                repository.getUserProfile(firebaseUser.uid).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            currentLoadedProfile = result.data
                            _userProfileState.value = result
                        }
                        is Result.Error -> {
                            val defaultProfile = UserProfile(
                                uid = firebaseUser.uid,
                                fullName = firebaseUser.displayName ?: "",
                                email = firebaseUser.email ?: ""
                            )
                            currentLoadedProfile = defaultProfile
                            _userProfileState.value = Result.Success(defaultProfile)
                        }
                        is Result.Loading -> {
                        }
                    }
                }
            }
        } else {
            _userProfileState.value = Result.Error("Pengguna tidak diautentikasi.")
        }
    }

    fun saveProfileName(fullName: String) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            _saveProfileResult.value = Result.Error("Pengguna tidak valid untuk menyimpan profil.")
            return
        }

        if (currentLoadedProfile == null) {
            _saveProfileResult.value = Result.Error("Tidak bisa menyimpan, data profil awal tidak ditemukan. Coba lagi.")
            return
        }

        val profileToSave = currentLoadedProfile!!.copy(
            fullName = fullName
        )

        viewModelScope.launch {
            repository.saveUserProfile(userId, profileToSave).collectLatest {
                _saveProfileResult.value = it
            }
        }
    }

    fun uploadAndSaveProfilePicture(imageUri: Uri) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            _saveProfileResult.value = Result.Error("Pengguna tidak valid.")
            return
        }

        viewModelScope.launch {
            repository.uploadProfileImage(userId, imageUri).collectLatest { uploadResult ->
                when (uploadResult) {
                    is Result.Loading -> _saveProfileResult.value = Result.Loading
                    is Result.Error -> _saveProfileResult.value = Result.Error(uploadResult.message)
                    is Result.Success -> {
                        val (newUrl, publicId) = uploadResult.data
                        val updatedProfile = currentLoadedProfile!!.copy(
                            profileImageUrl = newUrl,
                            profileImagePublicId = publicId
                        )
                        repository.saveUserProfile(userId, updatedProfile).collectLatest { saveResult ->
                            _saveProfileResult.value = saveResult
                            if (saveResult is Result.Success) loadUserProfile()
                        }
                    }
                }
            }
        }
    }

    fun deleteProfilePicture() {
        val userId = firebaseAuth.currentUser?.uid
        val profileToDelete = currentLoadedProfile
        val publicId = profileToDelete?.profileImagePublicId

        if (userId == null || publicId.isNullOrBlank()) {
            _saveProfileResult.value = Result.Error("Tidak ada gambar untuk dihapus.")
            return
        }

        viewModelScope.launch {
            _saveProfileResult.value = Result.Loading
            repository.deleteProfileImage(publicId).collectLatest { deleteResult ->
                if (deleteResult is Result.Success) {
                    val updatedProfile = profileToDelete.copy(
                        profileImageUrl = null,
                        profileImagePublicId = null
                    )
                    repository.saveUserProfile(userId, updatedProfile).collectLatest { saveResult ->
                        _saveProfileResult.value = saveResult
                        if (saveResult is Result.Success) loadUserProfile()
                    }
                } else if (deleteResult is Result.Error) {
                    _saveProfileResult.value = deleteResult
                }
            }
        }
    }
}