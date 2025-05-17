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
        holder.bind(kos)
    }

    inner class ListViewHolder(private val binding: ItemsKostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(kos: Kos) {
            try {
                // set data to view
                binding.nameKos.text = kos.nama_kost
                binding.categoryKos.text = kos.kategori
                binding.addressKos.text = kos.alamat
                binding.descriptionKos.text = kos.deskripsi

                // formatting price to currency IDR
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id","ID"))
                binding.priceKos.text = formatRupiah.format(kos.harga)

                if (kos.foto_kost.isNotEmpty()) {
                    Glide.with(binding.imageKos.context)
                        .load(kos.foto_kost[0])
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