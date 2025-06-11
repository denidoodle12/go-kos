package com.myskripsi.gokos.ui.activity.profile.editProfile

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
                            // Jika profil ditemukan di Firestore, gunakan itu.
                            currentLoadedProfile = result.data
                            _userProfileState.value = result
                        }
                        is Result.Error -> {
                            // --- INI BAGIAN PENTINGNYA ---
                            // Jika profil TIDAK DITEMUKAN di Firestore (kasus pengguna baru),
                            // jangan kirim error ke UI. Sebagai gantinya, buat profil default
                            // dari data Firebase Auth dan kirim sebagai Result.Success.
                            val defaultProfile = UserProfile(
                                uid = firebaseUser.uid,
                                fullName = firebaseUser.displayName ?: "",
                                email = firebaseUser.email ?: ""
                                // Atribut lain akan menggunakan nilai default dari data class
                            )
                            currentLoadedProfile = defaultProfile
                            _userProfileState.value = Result.Success(defaultProfile)
                        }
                        is Result.Loading -> {
                            // Biarkan loading state
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

        // Karena `loadUserProfile` sudah diubah, `currentLoadedProfile` sekarang
        // tidak akan pernah null di sini jika pengguna sudah login.
        if (currentLoadedProfile == null) {
            _saveProfileResult.value = Result.Error("Tidak bisa menyimpan, data profil awal tidak ditemukan. Coba lagi.")
            return
        }

        // Buat objek baru dengan menyalin data yang ada dan hanya mengubah nama
        val profileToSave = currentLoadedProfile!!.copy(
            fullName = fullName
        )

        viewModelScope.launch {
            repository.saveUserProfile(userId, profileToSave).collectLatest {
                _saveProfileResult.value = it
            }
        }
    }
}