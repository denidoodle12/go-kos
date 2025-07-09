package com.myskripsi.gokos.ui.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.FavoriteItemUI
import com.myskripsi.gokos.databinding.ItemsFavoriteKosBinding
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class FavoriteAdapter : ListAdapter<FavoriteItemUI, FavoriteAdapter.FavoriteViewHolder>(DIFF_CALLBACK) {

    var onItemClick: ((FavoriteItemUI) -> Unit)? = null
    var onRemoveClick: ((FavoriteItemUI) -> Unit)? = null
    var onNoteClick: ((FavoriteItemUI) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemsFavoriteKosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavoriteViewHolder(private val binding: ItemsFavoriteKosBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { getItem(absoluteAdapterPosition)?.let { onItemClick?.invoke(it) } }
            binding.ivFavorite.setOnClickListener { getItem(absoluteAdapterPosition)?.let { onRemoveClick?.invoke(it) } }
            binding.noteContainer.setOnClickListener { getItem(absoluteAdapterPosition)?.let { onNoteClick?.invoke(it) } }
            binding.ivEditNote.setOnClickListener { getItem(absoluteAdapterPosition)?.let { onNoteClick?.invoke(it) } }
        }

        fun bind(item: FavoriteItemUI) {
            val favorite = item.favorite
            val kos = item.kos

            binding.tvKosName.text = kos.nama_kost
            binding.tvAddress.text = kos.alamat
            binding.tvCategory.text = kos.kategori

            val allFacilities = kos.fasilitas_kamar + kos.fasilitas_kamar_mandi
            if (allFacilities.isNotEmpty()) {
                binding.tvFacilities.visibility = View.VISIBLE
                val facilitiesText = allFacilities.joinToString(" • ")
                binding.tvFacilities.text = facilitiesText
            } else {
                binding.tvFacilities.visibility = View.GONE
            }

            binding.tvPrice.text = CURRENCY_FORMATTER.format(kos.harga.toDouble())

            if (kos.listrik.trim().contains("Termasuk", ignoreCase = true)) {
                binding.tvElectricity.visibility = View.VISIBLE
            } else {
                binding.tvElectricity.visibility = View.GONE
            }

            Glide.with(itemView.context)
                .load(kos.foto_kost.firstOrNull())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(binding.imageKos)

            // --- NEW LOGIC FOR DISTANCE ---
            if (item.distance >= 0) {
                binding.tvDistance.visibility = View.VISIBLE
                binding.tvDistance.text = "${formatDistance(item.distance)} from you"
            } else {
                binding.tvDistance.visibility = View.GONE
            }

            // --- Note Logic ---
            binding.noteContainer.visibility = View.VISIBLE
            if (!favorite.note.isNullOrBlank()) {
                binding.tvNote.text = favorite.note
                binding.tvNote.setTypeface(null, Typeface.ITALIC)
            } else {
                binding.tvNote.text = "Tambahkan Catatan"
                binding.tvNote.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    companion object {
        val CURRENCY_FORMATTER: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        fun formatDistance(distanceInKm: Double): String {
            return if (distanceInKm < 1.0) {
                "${DecimalFormat("#").format(distanceInKm * 1000)} m"
            } else {
                "${DecimalFormat("#.#").format(distanceInKm)} km"
            }
        }
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FavoriteItemUI>() {
            override fun areItemsTheSame(oldItem: FavoriteItemUI, newItem: FavoriteItemUI): Boolean = oldItem.favorite.id == newItem.favorite.id
            override fun areContentsTheSame(oldItem: FavoriteItemUI, newItem: FavoriteItemUI): Boolean = oldItem == newItem
        }
    }
}