package com.myskripsi.gokos.ui.adapter

import android.view.LayoutInflater
import android.view.View
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

class KosAdapter(private var campusName: String = "") : ListAdapter<Kos, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    var onItemClick: ((Kos) -> Unit)? = null

    companion object {
        private const val VIEW_TYPE_REGULAR_KOS = 1
        private const val VIEW_TYPE_NEARBY_KOS = 2

        fun formatDistance(distanceInKm: Double): String {
            return if (distanceInKm < 0) {
                "Calculating..."
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

    fun setCampusName(name: String) {
        // Logika untuk menyingkat nama kampus
        this.campusName = when {
            name.contains("Serang Raya", ignoreCase = true) -> "Unsera"
            name.contains("Bina Bangsa", ignoreCase = true) -> "Uniba"
            else -> name
        }
        // Beritahu adapter untuk me-render ulang semua item yang terlihat
        notifyItemRangeChanged(0, itemCount)
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
                RegularKosViewHolder(binding)
            }
            VIEW_TYPE_NEARBY_KOS -> {
                val binding = ItemsNearbyKostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                NearbyKosViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val kos = getItem(position)
        if (kos != null) {
            when (holder) {
                is RegularKosViewHolder -> holder.bind(kos, onItemClick)
                is NearbyKosViewHolder -> holder.bind(kos, onItemClick)
            }
        }
    }

    inner class RegularKosViewHolder(private val binding: ItemsKostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(kos: Kos, onItemClick: ((Kos) -> Unit)?) {
            try {
                binding.tvKosName.text = kos.nama_kost
                binding.tvCategory.text = kos.kategori
                binding.tvAddress.text = kos.alamat

                val formattedDistance = formatDistance(kos.lokasi.jarak)
                if (campusName.isNotBlank()) {
                    binding.tvDistance.text = "$formattedDistance dari $campusName"
                } else {
                    binding.tvDistance.text = formattedDistance
                }

                val allFacilities = kos.fasilitas_kamar + kos.fasilitas_kamar_mandi

                if (allFacilities.isNotEmpty()) {
                    binding.tvFacilities.visibility = View.VISIBLE
                    val facilitiesText = allFacilities.joinToString("•")
                    binding.tvFacilities.text = facilitiesText
                }

                binding.tvPrice.text = CURRENCY_FORMATTER.format(kos.harga.toDouble())

                if (kos.listrik.trim().contains("Termasuk", ignoreCase = true)) {
                    binding.tvElectricity.visibility = View.VISIBLE
                } else {
                    binding.tvElectricity.visibility = View.GONE
                }

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

    inner class NearbyKosViewHolder(private val binding: ItemsNearbyKostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(kos: Kos, onItemClick: ((Kos) -> Unit)?) {
            try {
                binding.nameKos.text = kos.nama_kost
                binding.tvCategory.text = kos.kategori
                binding.addressKos.text = kos.alamat

                binding.textView.text = formatDistance(kos.lokasi.jarak)
                binding.startFrom.text = itemView.context.getString(R.string.txt_startFrom)

                val allFacilities = kos.fasilitas_kamar + kos.fasilitas_kamar_mandi

                if (allFacilities.isNotEmpty()) {
                    binding.facilitiesKos.visibility = View.VISIBLE
                    val facilitiesText = allFacilities.joinToString("•")
                    binding.facilitiesKos.text = facilitiesText
                }

                binding.priceKos.text = CURRENCY_FORMATTER.format(kos.harga.toDouble())

                if (kos.listrik.trim().contains("Termasuk", ignoreCase = true)) {
                    binding.tvElectricity.visibility = View.VISIBLE
                } else {
                    binding.tvElectricity.visibility = View.GONE
                }

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
}