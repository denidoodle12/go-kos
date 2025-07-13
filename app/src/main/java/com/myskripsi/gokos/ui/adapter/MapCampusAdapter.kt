package com.myskripsi.gokos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.Campus
import com.myskripsi.gokos.databinding.ItemsMapCampusBinding

class MapCampusAdapter : ListAdapter<Campus, MapCampusAdapter.CampusViewHolder>(DIFF_CALLBACK) {

    var onItemClick: ((Campus) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampusViewHolder {
        val binding = ItemsMapCampusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CampusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CampusViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CampusViewHolder(private val binding: ItemsMapCampusBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                getItem(absoluteAdapterPosition)?.let { campus -> onItemClick?.invoke(campus) }
            }
        }

        fun bind(campus: Campus) {
            binding.tvCampusName.text = campus.nama_kampus
            binding.tvCampusAddress.text = campus.alamat

            val mapImage = when {
                campus.nama_kampus.contains("FKIP") -> R.drawable.maps_fkipuntirta
                campus.nama_kampus.contains("UIN") -> R.drawable.maps_uinsmhbanten
                campus.nama_kampus.contains("Serang Raya") -> R.drawable.maps_unsera
                campus.nama_kampus.contains("Bina Bangsa") -> R.drawable.maps_uniba
                else -> R.drawable.placeholder_image
            }

            val descriptionText = itemView.context.getString(R.string.maps_campus_description, campus.nama_kampus)
            binding.tvDescriptionCampus.text = descriptionText

            binding.ivCampusMapImage.setImageResource(mapImage)

            Glide.with(itemView.context)
                .load(campus.logo_url)
                .placeholder(R.drawable.placeholder_image)
                .into(binding.ivCampusLogo)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Campus>() {
            override fun areItemsTheSame(oldItem: Campus, newItem: Campus): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Campus, newItem: Campus): Boolean = oldItem == newItem
        }
    }
}