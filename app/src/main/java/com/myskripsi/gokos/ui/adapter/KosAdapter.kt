package com.myskripsi.gokos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.databinding.ItemsKostBinding
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class KosAdapter : ListAdapter<Kos, KosAdapter.ListViewHolder>(DIFF_CALLBACK){

    var onItemClick:((Kos) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListViewHolder {
        val binding = ItemsKostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val kos = getItem(position)
        if (kos != null) {
            holder.bind(kos)
        }
    }

    inner class ListViewHolder(private val binding: ItemsKostBinding) : RecyclerView.ViewHolder(binding.root) {

        // Fungsi untuk memformat jarak, bisa juga diletakkan di luar class jika dipakai di banyak tempat
        private fun formatDistance(distanceInKm: Double): String {
            return if (distanceInKm < 0) { // Jika jarak belum dihitung (masih default dari model sebelum update)
                "Menghitung..." // Atau "N/A", atau biarkan kosong
            } else if (distanceInKm < 1.0) {
                val distanceInMeters = distanceInKm * 1000
                // Format tanpa desimal untuk meter
                "${DecimalFormat("#").format(distanceInMeters)} m"
            } else {
                // Format dengan satu angka desimal untuk kilometer
                "${DecimalFormat("#.#").format(distanceInKm)} km"
            }
        }

        fun bind(kos: Kos) {
            try {
                // set data to view
                binding.nameKos.text = kos.nama_kost
                binding.categoryKos.text = kos.kategori
                binding.addressKos.text = kos.alamat
                binding.descriptionKos.text = kos.deskripsi
                // binding.addressKos.text = kos.alamat // Alamat mungkin terlalu panjang untuk item list, opsional
                // binding.descriptionKos.text = kos.deskripsi // Deskripsi juga biasanya di detail, opsional

                // Menampilkan jarak yang sudah dihitung dan diformat dari ViewModel
                // kos.lokasi.jarak akan diisi oleh ListKosViewModel
                binding.distance.text = formatDistance(kos.lokasi.jarak)


                // formatting price to currency IDR
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id","ID"))
                binding.priceKos.text = formatRupiah.format(kos.harga.toDouble())

                if (kos.foto_kost.isNotEmpty()) {
                    Glide.with(binding.imageKos.context)
                        .load(kos.foto_kost[0])
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop() //
                        .into(binding.imageKos)
                } else {
                    binding.imageKos.setImageResource(R.drawable.placeholder_image)
                }

                itemView.setOnClickListener {
                    onItemClick?.invoke(kos)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Pertimbangkan untuk menampilkan UI error default jika ada masalah saat binding
            }
        }
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Kos> =
            object : DiffUtil.ItemCallback<Kos>() {
                override fun areItemsTheSame(oldItem: Kos, newItem: Kos): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: Kos, newItem: Kos): Boolean {
                    return oldItem == newItem
                }
            }
    }
}