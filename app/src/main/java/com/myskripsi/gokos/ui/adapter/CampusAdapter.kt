package com.myskripsi.gokos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.Campus
import com.myskripsi.gokos.databinding.ItemsCampusCardBinding

@Suppress("DEPRECATION")
class CampusAdapter : ListAdapter<Campus, CampusAdapter.CampusViewHolder>(CAMPUS_COMPARATOR) {

    var onItemClick: ((Campus) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampusViewHolder {
        val binding = ItemsCampusCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CampusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CampusViewHolder, position: Int) {
        val currentCampus = getItem(position)
        if (currentCampus != null) {
            holder.bind(currentCampus)
        }
    }

    inner class CampusViewHolder(private val binding: ItemsCampusCardBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    getItem(position)?.let { campus ->
                        onItemClick?.invoke(campus)
                    }
                }
            }
        }

        fun bind(campus: Campus) {
            binding.tvCampusName.text = campus.nama_kampus
            Glide.with(itemView.context)
                .load(campus.logo_url)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(binding.ivCampusLogo)
        }
    }

    companion object {
        private val CAMPUS_COMPARATOR = object : DiffUtil.ItemCallback<Campus>() {
            override fun areItemsTheSame(oldItem: Campus, newItem: Campus): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Campus, newItem: Campus): Boolean {
                return oldItem == newItem
            }
        }
    }
}