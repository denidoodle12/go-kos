package com.myskripsi.gokos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.databinding.ItemsKostBinding // Pastikan nama binding ini sesuai dengan file XML Anda
import java.text.DecimalFormat // Import DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class KosAdapter : ListAdapter<Kos, KosAdapter.ListViewHolder>(DIFF_CALLBACK){

    var onItemClick:((Kos) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListViewHolder {
        // Pastikan ItemsKostBinding adalah nama file binding yang benar untuk item layout Anda
        val binding = ItemsKostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val kos = getItem(position)
        if (kos != null) { // Tambahkan null check untuk getItem()
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
                binding.priceKos.text = formatRupiah.format(kos.harga.toDouble()) // Pastikan harga adalah numeric

                if (kos.foto_kost.isNotEmpty()) {
                    Glide.with(binding.imageKos.context)
                        .load(kos.foto_kost[0])
                        .placeholder(R.drawable.placeholder_image) // Tambahkan placeholder
                        .error(R.drawable.placeholder_image) // Tambahkan gambar jika terjadi error load
                        .centerCrop() // atau fitCenter(), sesuai kebutuhan desain
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
        // Nama DIFF_CALLBACK sudah benar
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Kos> =
            object : DiffUtil.ItemCallback<Kos>() {
                override fun areItemsTheSame(oldItem: Kos, newItem: Kos): Boolean {
                    return oldItem.id == newItem.id
                }

                // Membandingkan konten. Pastikan data class Kos sudah benar implementasi equals() nya
                // (Kotlin data class otomatis membuatkan implementasi equals yang baik).
                override fun areContentsTheSame(oldItem: Kos, newItem: Kos): Boolean {
                    return oldItem == newItem
                }
            }
    }
}