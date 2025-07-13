package com.myskripsi.gokos.ui.activity.detailKos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.AuthRepository
import com.myskripsi.gokos.data.FavoriteRepository
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Favorite
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.ui.adapter.KosLayoutType
import com.myskripsi.gokos.utils.HaversineHelper
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetailKosViewModel(
    private val kosRepository: KosRepository,
    private val authRepository: AuthRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _otherNearbyKosState = MutableLiveData<Result<List<Kos>>>()
    val otherNearbyKosState: LiveData<Result<List<Kos>>> = _otherNearbyKosState

    private val _favoriteStatus = MutableLiveData<Result<Favorite?>>()
    val favoriteStatus: LiveData<Result<Favorite?>> = _favoriteStatus

    private val _actionResult = MutableLiveData<Result<String>>()
    val actionResult: LiveData<Result<String>> = _actionResult

    fun fetchOtherNearbyKos(mainKos: Kos) {
        viewModelScope.launch {
            _otherNearbyKosState.value = Result.Loading

            kosRepository.getAllKos().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val allKosList = result.data
                        val otherKosWithDistance = allKosList
                            .filter { it.id != mainKos.id }
                            .map { otherKos ->
                                val distance = HaversineHelper.calculateDistance(
                                    mainKos.lokasi.latitude, mainKos.lokasi.longitude,
                                    otherKos.lokasi.latitude, otherKos.lokasi.longitude
                                )
                                otherKos.copy(
                                    lokasi = otherKos.lokasi.copy(jarak = distance),
                                    layoutType = KosLayoutType.NEARBY
                                )
                            }
                            .sortedBy { it.lokasi.jarak }
                            .take(5)

                        _otherNearbyKosState.value = Result.Success(otherKosWithDistance)
                    }
                    is Result.Error -> {
                        _otherNearbyKosState.value = Result.Error("Gagal memuat data kos lain: ${result.message}")
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    fun checkFavoriteStatus(kosId: String) {
        val userId = authRepository.getCurrentUser()?.uid
        if (userId == null) {
            _favoriteStatus.value = Result.Error("Pengguna belum login.")
            return
        }
        viewModelScope.launch {
            favoriteRepository.isFavorited(userId, kosId).collectLatest {
                _favoriteStatus.value = it
            }
        }
    }

    fun addFavorite(kos: Kos, note: String) {
        val userId = authRepository.getCurrentUser()?.uid
        if (userId == null) {
            _actionResult.value = Result.Error("Gagal menyimpan, pengguna belum login.")
            return
        }

        // Buat objek Favorite yang ramping
        val favorite = Favorite(
            userId = userId,
            kosId = kos.id,
            note = note.ifBlank { null }
        )

        viewModelScope.launch {
            favoriteRepository.addFavorite(favorite).collectLatest { result ->
                when(result) {
                    is Result.Success -> {
                        _actionResult.value = Result.Success("Berhasil disimpan ke favorit!")
                        checkFavoriteStatus(kos.id) // Refresh status
                    }
                    is Result.Error -> _actionResult.value = Result.Error(result.message)
                    is Result.Loading -> _actionResult.value = Result.Loading
                }
            }
        }
    }

    fun removeFavorite(favoriteId: String, kosId: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(favoriteId).collectLatest { result ->
                when(result) {
                    is Result.Success -> {
                        _actionResult.value = Result.Success("Berhasil dihapus dari favorit.")
                        checkFavoriteStatus(kosId)
                    }
                    is Result.Error -> _actionResult.value = Result.Error(result.message)
                    is Result.Loading -> _actionResult.value = Result.Loading
                }
            }
        }
    }


}