package com.myskripsi.gokos.ui.activity.detailKos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.databinding.ActivityDetailKosBinding

@Suppress("DEPRECATION")
class DetailKosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailKosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailKosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataKos = intent.getParcelableExtra<Kos>(EXTRA_DETAIL_KOS)
        if (dataKos != null) {
            showData(dataKos)
            Log.d("DetailKos", "DataKos: $dataKos")
        }
    }

    private fun showData(dataKos: Kos) {
        if (dataKos.foto_kost.isNotEmpty()) {
            Glide.with(this)
                .load(dataKos.foto_kost[0])
                .into(binding.ivKosMainImage)
        } else {
            binding.ivKosMainImage.setImageResource(R.drawable.placeholder_image)
        }

        binding.tvDistance.text = dataKos.lokasi.jarak.toString()
        binding.tvKosType.text = dataKos.kategori
        binding.tvKosName.text = dataKos.nama_kost
        binding.tvKosAddress.text = dataKos.alamat
        binding.tvKosDescription.text = dataKos.deskripsi
        binding.tvListrik.text = dataKos.listrik
        binding.tvLatitude.text = dataKos.lokasi.latitude.toString()
        binding.tvLongitude.text = dataKos.lokasi.longitude.toString()

        val fasilitasKamarText = dataKos.fasilitas_kamar.joinToString(", ")
        binding.tvFasilitasKamar.text = fasilitasKamarText

        val fasilitasKamarMandiText = dataKos.fasilitas_kamar_mandi.joinToString(", ")
        binding.tvFasilitasKamarMandi.text = fasilitasKamarMandiText
    }


    companion object {
        const val EXTRA_DETAIL_KOS = "extra_detail_kos"
    }
}