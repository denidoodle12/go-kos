package com.myskripsi.gokos.ui.activity.detailKos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.ui.adapter.KosLayoutType
import com.myskripsi.gokos.utils.HaversineHelper
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetailKosViewModel(private val repository: KosRepository) : ViewModel() {

    private val _otherNearbyKosState = MutableLiveData<Result<List<Kos>>>()
    val otherNearbyKosState: LiveData<Result<List<Kos>>> = _otherNearbyKosState

    /**
     * Mengambil semua data kos, lalu memfilternya untuk menemukan
     * kos lain yang terdekat dari kos utama yang sedang ditampilkan.
     *
     * @param mainKos adalah objek kos yang sedang dilihat di halaman detail.
     */
    fun fetchOtherNearbyKos(mainKos: Kos) {
        viewModelScope.launch {
            _otherNearbyKosState.value = Result.Loading // Set status loading

            // Ambil semua data kos dari repository
            repository.getAllKos().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        // Jika berhasil, proses data untuk mencari yang terdekat
                        val allKosList = result.data
                        val otherKosWithDistance = allKosList
                            // 1. Filter untuk membuang kos utama dari daftar
                            .filter { it.id != mainKos.id }
                            // 2. Hitung jarak setiap kos lain dari kos utama
                            .map { otherKos ->
                                val distance = HaversineHelper.calculateDistance(
                                    mainKos.lokasi.latitude, mainKos.lokasi.longitude,
                                    otherKos.lokasi.latitude, otherKos.lokasi.longitude
                                )
                                // 3. Buat objek baru dengan jarak yang sudah dihitung dan set layoutType
                                otherKos.copy(
                                    lokasi = otherKos.lokasi.copy(jarak = distance),
                                    layoutType = KosLayoutType.NEARBY // Gunakan layout nearby
                                )
                            }
                            // 4. Urutkan berdasarkan jarak terdekat
                            .sortedBy { it.lokasi.jarak }
                            // 5. Ambil 5 teratas (atau sesuaikan jumlahnya)
                            .take(5)

                        _otherNearbyKosState.value = Result.Success(otherKosWithDistance)
                    }
                    is Result.Error -> {
                        // Jika gagal, kirim status error
                        _otherNearbyKosState.value = Result.Error("Gagal memuat data kos lain: ${result.message}")
                    }
                    is Result.Loading -> { /* Biarkan, karena sudah di-handle di awal */ }
                }
            }
        }
    }
}