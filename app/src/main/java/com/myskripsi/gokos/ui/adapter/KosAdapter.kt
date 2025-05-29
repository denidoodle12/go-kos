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
import com.myskripsi.gokos.databinding.ItemsNearbyKostBinding
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class KosAdapter : ListAdapter<Kos, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    var onItemClick: ((Kos) -> Unit)? = null

    companion object {
        private const val VIEW_TYPE_REGULAR_KOS = 1
        private const val VIEW_TYPE_NEARBY_KOS = 2

        fun formatDistance(distanceInKm: Double): String {
            return if (distanceInKm < 0) {
                "Menghitung..."
            } else if (distanceInKm < 1.0) {
                val distanceInMeters = distanceInKm * 1000
                "${DecimalFormat("#").format(distanceInMeters)} m"
            } else {
                "${DecimalFormat("#.#").format(distanceInKm)} km"
            }
        }

        val CURRENCY_FORMATTER: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

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

    override fun getItemViewType(position: Int): Int {
        val kos = getItem(position)
        return if (kos.layoutType == KosLayoutType.NEARBY) {
            VIEW_TYPE_NEARBY_KOS
        } else {
            VIEW_TYPE_REGULAR_KOS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_REGULAR_KOS -> {
                val binding = ItemsKostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                RegularKostViewHolder(binding)
            }
            VIEW_TYPE_NEARBY_KOS -> {
                val binding = ItemsNearbyKostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                NearbyKostViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val kos = getItem(position)
        if (kos != null) {
            when (holder) {
                is RegularKostViewHolder -> holder.bind(kos, onItemClick)
                is NearbyKostViewHolder -> holder.bind(kos, onItemClick)
            }
        }
    }

    inner class RegularKostViewHolder(private val binding: ItemsKostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(kos: Kos, onItemClick: ((Kos) -> Unit)?) {
            try {
                binding.nameKos.text = kos.nama_kost
                binding.categoryKos.text = kos.kategori
                binding.addressKos.text = kos.alamat
                binding.descriptionKos.text = kos.deskripsi ?: "" // Handle jika deskripsi bisa null

                binding.distance.text = formatDistance(kos.lokasi.jarak) // Asumsi 'distance' adalah ID di ItemsKostBinding
                binding.priceKos.text = CURRENCY_FORMATTER.format(kos.harga.toDouble() ?: 0.0)

                if (kos.foto_kost.isNotEmpty()) {
                    Glide.with(binding.imageKos.context)
                        .load(kos.foto_kost[0])
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(binding.imageKos)
                } else {
                    binding.imageKos.setImageResource(R.drawable.placeholder_image)
                }

                itemView.setOnClickListener {
                    onItemClick?.invoke(kos)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ViewHolder untuk layout items_nearby_kost.xml
    inner class NearbyKostViewHolder(private val binding: ItemsNearbyKostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(kos: Kos, onItemClick: ((Kos) -> Unit)?) {
            try {
                binding.nameKos.text = kos.nama_kost            // Asumsi ID: nameKos
                binding.categoryKos.text = kos.kategori        // Asumsi ID: categoryKos
                binding.addressKos.text = kos.alamat           // Asumsi ID: addressKos

                // Untuk 'items_nearby_kost.xml' yang Anda berikan sebelumnya:
                // - Jarak menggunakan ID 'textView'
                // - Ada 'startFrom'
                binding.textView.text = formatDistance(kos.lokasi.jarak) // ID: textView untuk jarak
                binding.startFrom.text = itemView.context.getString(R.string.txt_startFrom) // Jika "Mulai Dari" statis

                binding.priceKos.text = CURRENCY_FORMATTER.format(kos.harga.toDouble() ?: 0.0) // Asumsi ID: priceKos

                if (kos.foto_kost.isNotEmpty()) {
                    Glide.with(binding.imageKos.context)
                        .load(kos.foto_kost[0])
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(binding.imageKos) // Asumsi ID: imageKos
                } else {
                    binding.imageKos.setImageResource(R.drawable.placeholder_image)
                }

                itemView.setOnClickListener {
                    onItemClick?.invoke(kos)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}