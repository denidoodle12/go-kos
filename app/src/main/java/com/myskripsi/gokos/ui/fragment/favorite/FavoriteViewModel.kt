package com.myskripsi.gokos.ui.fragment.favorite

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.AuthRepository
import com.myskripsi.gokos.data.FavoriteRepository
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Favorite
import com.myskripsi.gokos.data.model.FavoriteItemUI
import com.myskripsi.gokos.utils.HaversineHelper
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

    // Updated to accept a nullable Location parameter
    fun loadUserFavorites(userLocation: Location? = null) {
        val userId = authRepository.getCurrentUser()?.uid
        if (userId == null) {
            _favoritesState.value = Result.Error("Anda harus login untuk melihat favorit.")
            return
        }

        _favoritesState.value = Result.Loading
        viewModelScope.launch {
            favoriteRepository.getFavorites(userId).collectLatest { favResult ->
                if (favResult is Result.Success) {
                    val favoriteObjects = favResult.data
                    if (favoriteObjects.isEmpty()) {
                        _favoritesState.value = Result.Success(emptyList()); return@collectLatest
                    }
                    try {
                        val favoriteUIList = favoriteObjects.map { favorite ->
                            async {
                                val kosResult = kosRepository.getKosById(favorite.kosId)
                                if (kosResult is Result.Success) {
                                    val kos = kosResult.data
                                    // Calculate distance if location is available
                                    val distance = if (userLocation != null) {
                                        HaversineHelper.calculateDistance(
                                            userLocation.latitude, userLocation.longitude,
                                            kos.lokasi.latitude, kos.lokasi.longitude
                                        )
                                    } else { -1.0 }
                                    val kosWithUpdatedDistance = kos.copy(
                                        lokasi = kos.lokasi.copy(jarak = distance)
                                    )

                                    // Buat FavoriteItemUI dengan objek Kos yang baru
                                    FavoriteItemUI(favorite, kosWithUpdatedDistance, distance)
                                } else { null }
                            }
                        }.awaitAll().filterNotNull()

                        // Sort the list by distance if location is available
                        val sortedList = if (userLocation != null) favoriteUIList.sortedBy { it.distance } else favoriteUIList
                        _favoritesState.value = Result.Success(sortedList)

                    } catch (e: Exception) {
                        _favoritesState.value = Result.Error("Gagal memuat detail kos favorit.")
                    }
                } else if (favResult is Result.Error) {
                    _favoritesState.value = Result.Error(favResult.message)
                }
            }
        }
    }

    // Updated to call loadUserFavorites without location
    fun updateFavoriteNote(favoriteId: String, newNote: String) {
        viewModelScope.launch {
            favoriteRepository.updateNote(favoriteId, newNote).collectLatest {
                if (it is Result.Success) loadUserFavorites()
            }
        }
    }

    // Updated to call loadUserFavorites without location
    fun removeFavorite(favorite: Favorite) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(favorite.id).collectLatest {
                if(it is Result.Success) {
                    _removeResult.value = Result.Success("Berhasil dihapus dari favorit.")
                    loadUserFavorites()
                } else if (it is Result.Error) {
                    _removeResult.value = it
                }
            }
        }
    }
}