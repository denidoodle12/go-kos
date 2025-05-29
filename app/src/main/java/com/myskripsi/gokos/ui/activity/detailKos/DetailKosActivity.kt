package com.myskripsi.gokos.ui.activity.detailKos // Pastikan package sesuai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.databinding.ActivityDetailKosBinding
import com.myskripsi.gokos.databinding.ItemsFacilityBinding
import java.text.DecimalFormat // Untuk format jarak
import java.util.Locale

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
        return if (distanceInKm < 0) {
            "N/A"
        } else if (distanceInKm < 1.0) {
            val distanceInMeters = distanceInKm * 1000
            "${DecimalFormat("#0").format(distanceInMeters)} m"
        } else {
            "${DecimalFormat("#0.0").format(distanceInKm)} km"
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

        binding.tvDistance.text = formatDistance(dataKos.lokasi.jarak)
        binding.tvKosType.text = dataKos.kategori
        binding.tvKosName.text = dataKos.nama_kost
        binding.tvKosAddress.text = dataKos.alamat
        binding.tvKosDescription.text = dataKos.deskripsi
        binding.tvListrik.text = dataKos.listrik

        binding.tvLatitude.text = "Lat: ${dataKos.lokasi.latitude}"
        binding.tvLongitude.text = "Lng: ${dataKos.lokasi.longitude}"

        populateFacilities(
            binding.llFasilitasKamarContainer,
            dataKos.fasilitas_kamar,
            binding.tvFasilitasKamarTitle
        )

        populateFacilities(
            binding.llFasilitasKamarMandiContainer, // ID dari XML yang diperbarui
            dataKos.fasilitas_kamar_mandi,
            binding.tvFasilitasKamarMandiTitle // ID dari XML yang diperbarui
        )
//
//        val fasilitasKamarText = if (dataKos.fasilitas_kamar.isEmpty()) "-" else dataKos.fasilitas_kamar.joinToString(", ")
//        binding.tvFasilitasKamar.text = fasilitasKamarText
//
//        val fasilitasKamarMandiText = if (dataKos.fasilitas_kamar_mandi.isEmpty()) "-" else dataKos.fasilitas_kamar_mandi.joinToString(", ")
//        binding.tvFasilitasKamarMandi.text = fasilitasKamarMandiText
    }

    private fun populateFacilities(
        container: LinearLayout,
        facilities: List<String>,
        titleView: TextView
    ) {
        container.removeAllViews()

        if (facilities.isEmpty()) {
            titleView.visibility = View.GONE
            container.visibility = View.GONE
            if (container.id == binding.llFasilitasKamarMandiContainer.id &&
                (binding.llFasilitasKamarContainer.visibility == View.GONE || binding.llFasilitasKamarContainer.childCount == 0)) {
                binding.divider3.visibility = View.GONE
            }
            return
        }

        titleView.visibility = View.VISIBLE
        container.visibility = View.VISIBLE
        if (binding.llFasilitasKamarContainer.childCount > 0 && binding.llFasilitasKamarMandiContainer.childCount > 0){
            binding.divider3.visibility = View.VISIBLE
        } else if (binding.tvFasilitasKamarTitle.visibility == View.VISIBLE && binding.tvFasilitasKamarMandiTitle.visibility == View.VISIBLE){
            binding.divider3.visibility = View.VISIBLE
        }


        val inflater = LayoutInflater.from(this)
        for (facilityName in facilities) {
            val facilityItemBinding = ItemsFacilityBinding.inflate(inflater, container, false)
            facilityItemBinding.tvFacilityName.text = facilityName
            facilityItemBinding.ivFacilityIcon.setImageResource(getIconForFacility(facilityName))
            container.addView(facilityItemBinding.root)
        }
    }

    private fun getIconForFacility(facilityName: String): Int {
        val normalizedName = facilityName.trim().lowercase(Locale.getDefault())
            .replace(" ", "")
            .replace("-", "")
            .replace("/", "")

        return when {
            normalizedName.contains("kasur") -> R.drawable.ic_facility_bed
            normalizedName.contains("jendela") -> R.drawable.ic_facility_window
            normalizedName.contains("bantal") || normalizedName.contains("guling") -> R.drawable.ic_facility_pillow
            normalizedName.contains("dapurdidalam") -> R.drawable.ic_facility_kitchen
            normalizedName.contains("lemaribaju") -> R.drawable.ic_facility_wardrobe
            normalizedName.contains("kulkas") -> R.drawable.ic_facility_kitchen
            normalizedName.contains("kosongan") -> R.drawable.ic_close_panel

            normalizedName.contains("kamarmandididalam") -> R.drawable.ic_facility_bathroom
            normalizedName.contains("kamarmandidiluar") -> R.drawable.ic_facility_bathroom
            normalizedName.contains("wcjongkok") -> R.drawable.ic_facility_wc
            normalizedName.contains("wcduduk") -> R.drawable.ic_facility_wc
            normalizedName.contains("embermandi") -> R.drawable.ic_facility_bucket
            normalizedName.contains("gayung") -> R.drawable.ic_facility_dipper
            else -> R.drawable.ic_close_panel
        }
    }


    companion object {
        const val EXTRA_DETAIL_KOS = "extra_detail_kos"
    }
}