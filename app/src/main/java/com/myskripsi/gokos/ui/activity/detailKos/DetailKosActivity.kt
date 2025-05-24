package com.myskripsi.gokos.ui.activity.detailKos // Pastikan package sesuai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.databinding.ActivityDetailKosBinding
import java.text.DecimalFormat // Untuk format jarak

@Suppress("DEPRECATION")
class DetailKosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailKosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailKosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ActionBar (Opsional, jika ingin judul dan tombol kembali)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            // title = "Detail Kos" // atau ambil dari dataKos.nama_kost
        }


        val dataKos = intent.getParcelableExtra<Kos>(EXTRA_DETAIL_KOS)
        if (dataKos != null) {
            showData(dataKos)
            Log.d("DetailKos", "DataKos: $dataKos, Jarak: ${dataKos.lokasi.jarak}")
        } else {
            Log.e("DetailKos", "Data Kos tidak ditemukan di Intent.")
            // Handle error, misalnya tampilkan pesan atau tutup activity
            finish()
        }
    }

    // Untuk tombol kembali di ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun formatDistance(distanceInKm: Double): String {
        return if (distanceInKm < 1) {
            val distanceInMeters = distanceInKm * 1000
            "${DecimalFormat("#").format(distanceInMeters)} m"
        } else {
            "${DecimalFormat("#.#").format(distanceInKm)} km"
        }
    }

    private fun showData(dataKos: Kos) {
        supportActionBar?.title = dataKos.nama_kost // Set judul ActionBar

        if (dataKos.foto_kost.isNotEmpty()) {
            Glide.with(this)
                .load(dataKos.foto_kost[0])
                .placeholder(R.drawable.placeholder_image) // Tambahkan placeholder
                .error(R.drawable.placeholder_image) // Tambahkan error image
                .into(binding.ivKosMainImage)
        } else {
            binding.ivKosMainImage.setImageResource(R.drawable.placeholder_image)
        }

        // Menampilkan jarak yang sudah dihitung dan diformat
        binding.tvDistance.text = formatDistance(dataKos.lokasi.jarak)
        binding.tvKosType.text = dataKos.kategori
        binding.tvKosName.text = dataKos.nama_kost
        binding.tvKosAddress.text = dataKos.alamat
        binding.tvKosDescription.text = dataKos.deskripsi
        binding.tvListrik.text = dataKos.listrik

        // Menampilkan latitude dan longitude (jika masih diperlukan untuk debugging atau info)
        // Jika tidak, Anda bisa menghapusnya dari layout detail
        binding.tvLatitude.text = "Lat: ${dataKos.lokasi.latitude}"
        binding.tvLongitude.text = "Lng: ${dataKos.lokasi.longitude}"


        val fasilitasKamarText = if (dataKos.fasilitas_kamar.isEmpty()) "-" else dataKos.fasilitas_kamar.joinToString(", ")
        binding.tvFasilitasKamar.text = fasilitasKamarText

        val fasilitasKamarMandiText = if (dataKos.fasilitas_kamar_mandi.isEmpty()) "-" else dataKos.fasilitas_kamar_mandi.joinToString(", ")
        binding.tvFasilitasKamarMandi.text = fasilitasKamarMandiText
    }


    companion object {
        const val EXTRA_DETAIL_KOS = "extra_detail_kos"
    }
}