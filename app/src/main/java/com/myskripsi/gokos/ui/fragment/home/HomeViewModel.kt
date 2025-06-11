package com.myskripsi.gokos.ui.fragment.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Kos
import android.location.Location
import com.myskripsi.gokos.data.AuthRepository
import com.myskripsi.gokos.data.model.Campus
import com.myskripsi.gokos.ui.adapter.KosLayoutType
import com.myskripsi.gokos.utils.HaversineHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.myskripsi.gokos.utils.Result

enum class PriceRangeFilter {
    BELOW_500K,
    BETWEEN_500K_700K,
    ABOVE_700K
}

class HomeViewModel(private val repository: KosRepository, private val authRepository: AuthRepository) : ViewModel() {
    private val _nearbyKosState = MutableLiveData<Result<List<Kos>>>()
    val nearbyKosState: LiveData<Result<List<Kos>>> = _nearbyKosState

    private val _campusListState = MutableLiveData<Result<List<Campus>>>()
    val campusListState: LiveData<Result<List<Campus>>> = _campusListState

    private val _userLocation = MutableLiveData<Location?>()
    val userLocation: LiveData<Location?> = _userLocation

    private val _userDisplayName = MutableLiveData<String?>()
    val userDisplayName: LiveData<String?> = _userDisplayName

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> = _userEmail

    private val _allKosList = MutableLiveData<List<Kos>>()

    private val _budgetKosState = MutableLiveData<Result<List<Kos>>>()
    val budgetKosState: LiveData<Result<List<Kos>>> = _budgetKosState

    private val _selectedPriceRange = MutableLiveData<PriceRangeFilter>()
    val selectedPriceRange: LiveData<PriceRangeFilter> = _selectedPriceRange


    fun loadUserProfile() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _userDisplayName.value = currentUser.displayName
            _userEmail.value = currentUser.email
        } else {
            _userDisplayName.value = null
            _userEmail.value = null
        }
    }

    fun updateUserLocation(location: Location?) {
        _userLocation.value = location
        if (location != null) {
            fetchNearbyKos(location.latitude, location.longitude)
        } else {
            _nearbyKosState.value = Result.Error("User location not found to search for nearby kos!.")
        }
    }

    fun fetchCampusList() {
        viewModelScope.launch {
            repository.getAllCampuses().collectLatest { result ->
                _campusListState.value = result
            }
        }
    }

    // Modifikasi fetchNearbyKos untuk menyimpan semua kos
    private fun fetchNearbyKos(userLat: Double, userLon: Double) {
        viewModelScope.launch {
            _nearbyKosState.value = Result.Loading
            repository.getAllKos().collectLatest { result -> // Asumsi getAllKos mengambil semua data kos
                when (result) {
                    is Result.Success -> {
                        val allKosListFromRepo = result.data
                        _allKosList.value = allKosListFromRepo // Simpan semua kos

                        if (allKosListFromRepo.isEmpty()) {
                            _nearbyKosState.value = Result.Success(emptyList())
                            // Jika filter budget belum diinisialisasi, mungkin bisa panggil filter default di sini
                            // atau pastikan _allKosList terisi sebelum filter budget pertama kali dipanggil
                            if(_selectedPriceRange.value != null && _budgetKosState.value == null){
                                filterKosByBudget(_selectedPriceRange.value!!)
                            } else if (_budgetKosState.value == null) { // Jika belum ada filter terpilih, filter default
                                filterKosByBudget(PriceRangeFilter.BELOW_500K)
                            }
                            return@collectLatest
                        }

                        val kosWithDistance = allKosListFromRepo.map { kos ->
                            val distance = HaversineHelper.calculateDistance(
                                userLat,
                                userLon,
                                kos.lokasi.latitude,
                                kos.lokasi.longitude
                            )
                            kos.copy(
                                lokasi = kos.lokasi.copy(jarak = distance),
                                layoutType = KosLayoutType.NEARBY // Atau sesuaikan jika perlu
                            )
                        }

                        val sortedNearbyKos = kosWithDistance.sortedBy { it.lokasi.jarak }
                        _nearbyKosState.value = Result.Success(sortedNearbyKos.take(5))

                        // Setelah _allKosList terisi, panggil filter budget jika ada filter terpilih atau default
                        if(_selectedPriceRange.value != null){
                            filterKosByBudget(_selectedPriceRange.value!!)
                        } else { // Filter default jika belum ada yg terpilih
                            filterKosByBudget(PriceRangeFilter.BELOW_500K)
                        }
                    }
                    is Result.Error -> {
                        _nearbyKosState.value = Result.Error(result.message)
                        _budgetKosState.value = Result.Error("Failed to load base data for budget filtering: ${result.message}") // Juga error untuk budget
                    }
                    is Result.Loading -> {
                        _nearbyKosState.value = Result.Loading
                        // Bisa juga set _budgetKosState ke Loading jika _allKosList belum ada
                        if (_allKosList.value.isNullOrEmpty()) {
                            _budgetKosState.value = Result.Loading
                        }
                    }
                }
            }
        }
    }

    fun filterKosByBudget(priceRange: PriceRangeFilter) {
        _selectedPriceRange.value = priceRange // Tetap set filter yang dipilih
        _budgetKosState.value = Result.Loading // Set status loading

        viewModelScope.launch {
            val currentAllKos = _allKosList.value
            val currentUserLocation = _userLocation.value // Ambil lokasi pengguna saat ini

            if (currentAllKos.isNullOrEmpty()) {
                // Jika _allKosList kosong (misal data dasar belum termuat atau memang tidak ada)
                if (nearbyKosState.value is Result.Success || nearbyKosState.value is Result.Error) {
                    // Jika proses fetchNearbyKos (yang mengisi _allKosList) sudah selesai (sukses atau error)
                    _budgetKosState.value = Result.Success(emptyList())
                }
                // Jika nearbyKosState masih loading, _budgetKosState juga akan diupdate
                // nanti saat _allKosList terisi melalui panggilan filterKosByBudget dari fetchNearbyKos.
                return@launch
            }

            val filteredList = currentAllKos.filter { kos ->
                // Logika filter harga tetap sama
                when (priceRange) {
                    PriceRangeFilter.BELOW_500K -> kos.harga < 500000
                    PriceRangeFilter.BETWEEN_500K_700K -> kos.harga >= 500000 && kos.harga <= 700000
                    PriceRangeFilter.ABOVE_700K -> kos.harga > 700000
                }
            }.map { kos ->
                var kosOutput = kos // Mulai dengan objek kos asli dari hasil filter harga

                // 1. Hitung jarak jika lokasi pengguna tersedia
                currentUserLocation?.let { location ->
                    if (kos.lokasi.latitude != 0.0 || kos.lokasi.longitude != 0.0) { // Hanya hitung jika kos punya koordinat valid
                        val distance = HaversineHelper.calculateDistance(
                            location.latitude,
                            location.longitude,
                            kos.lokasi.latitude,
                            kos.lokasi.longitude
                        )
                        // Perbarui jarak pada objek kos
                        kosOutput = kosOutput.copy(lokasi = kosOutput.lokasi.copy(jarak = distance))
                    } else {
                        // Jika kos tidak punya koordinat, set jarak ke nilai default (misal -1 atau biarkan 0.0 agar adapter bisa handle)
                        kosOutput = kosOutput.copy(lokasi = kosOutput.lokasi.copy(jarak = -1.0)) // Contoh, agar bisa diformat sebagai "N/A"
                    }
                }

                // 2. Set layoutType agar menggunakan NearbyKosViewHolder
                kosOutput.copy(layoutType = KosLayoutType.NEARBY)
            }

            _budgetKosState.value = Result.Success(filteredList)
        }
    }

}