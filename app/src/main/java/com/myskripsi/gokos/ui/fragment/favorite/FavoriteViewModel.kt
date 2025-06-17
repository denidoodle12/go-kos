// File: ui/fragment/favorite/FavoriteViewModel.kt
package com.myskripsi.gokos.ui.fragment.favorite

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.AuthRepository
import com.myskripsi.gokos.data.FavoriteRepository
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Favorite
import com.myskripsi.gokos.data.model.FavoriteItemUI
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val kosRepository: KosRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _favoritesState = MutableLiveData<Result<List<FavoriteItemUI>>>()
    val favoritesState: LiveData<Result<List<FavoriteItemUI>>> = _favoritesState

    private val _removeResult = MutableLiveData<Result<String>>()
    val removeResult: LiveData<Result<String>> = _removeResult

    fun loadUserFavorites() {
        val userId = authRepository.getCurrentUser()?.uid
        if (userId == null) {
            _favoritesState.value = Result.Error("Anda harus login untuk melihat favorit.")
            return
        }

        _favoritesState.value = Result.Loading
        viewModelScope.launch {
            favoriteRepository.getFavorites(userId).collectLatest { favResult ->
                when (favResult) {
                    is Result.Success -> {
                        val favoriteObjects = favResult.data
                        if (favoriteObjects.isEmpty()) {
                            _favoritesState.value = Result.Success(emptyList())
                            return@collectLatest
                        }

                        try {
                            val favoriteUIList = favoriteObjects.map { favorite ->
                                async {
                                    // Memanggil suspend fun secara langsung, jauh lebih sederhana!
                                    val kosResult = kosRepository.getKosById(favorite.kosId)
                                    if (kosResult is Result.Success) {
                                        FavoriteItemUI(favorite, kosResult.data)
                                    } else {
                                        null
                                    }
                                }
                            }.awaitAll().filterNotNull()

                            _favoritesState.value = Result.Success(favoriteUIList)

                        } catch (e: Exception) {
                            _favoritesState.value = Result.Error("Gagal memuat detail kos favorit: ${e.message}")
                        }
                    }
                    is Result.Error -> _favoritesState.value = Result.Error(favResult.message)
                    is Result.Loading -> {}
                }
            }
        }
    }

    fun updateFavoriteNote(favoriteId: String, newNote: String) {
        viewModelScope.launch {
            // Kita bisa langsung panggil update dan muat ulang daftar jika berhasil
            favoriteRepository.updateNote(favoriteId, newNote).collectLatest { result ->
                if (result is Result.Success) {
                    // Beri feedback ke UI jika perlu, atau langsung muat ulang
                    loadUserFavorites()
                } else if (result is Result.Error) {
                    // Kirim event error ke UI jika perlu
                    // _updateError.value = result.message
                }
            }
        }
    }

    fun removeFavorite(favorite: Favorite) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(favorite.id).collectLatest { result ->
                when(result) {
                    is Result.Success -> {
                        _removeResult.value = Result.Success("Berhasil dihapus dari favorit.")
                        loadUserFavorites()
                    }
                    is Result.Error -> _removeResult.value = Result.Error(result.message)
                    is Result.Loading -> {}
                }
            }
        }
    }
}