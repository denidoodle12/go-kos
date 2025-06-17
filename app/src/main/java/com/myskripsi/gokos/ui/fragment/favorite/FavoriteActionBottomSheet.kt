// File: ui/fragment/favorite/FavoriteActionBottomSheet.kt
package com.myskripsi.gokos.ui.fragment.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.databinding.FragmentFavoriteActionBinding

class FavoriteActionBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentFavoriteActionBinding? = null
    private val binding get() = _binding!!

    private var listener: FavoriteActionListener? = null

    private lateinit var kos: Kos
    private lateinit var mode: Mode
    private var existingNote: String? = null

    enum class Mode { ADD, DELETE }

    interface FavoriteActionListener {
        fun onSaveClicked(kos: Kos, note: String)
        fun onDeleteClicked(kos: Kos)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoriteActionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data dari arguments
        arguments?.let {
            kos = it.getParcelable(ARG_KOS)!!
            mode = it.getSerializable(ARG_MODE) as Mode
            existingNote = it.getString(ARG_NOTE)
        }

        setupView()
        setupListeners()
    }

    private fun setupView() {
        binding.tvKosName.text = kos.nama_kost
        binding.tvKosAddress.text = kos.alamat

        if (kos.foto_kost.isNotEmpty()) {
            Glide.with(this).load(kos.foto_kost[0]).into(binding.ivKosImage)
        } else {
            binding.ivKosImage.setImageResource(R.drawable.placeholder_image)
        }

        if (mode == Mode.ADD) {
            binding.tvTitle.text = "Simpan ke Favorit"
            binding.tilNote.visibility = View.VISIBLE
            binding.etNote.setText(existingNote)
            binding.btnConfirm.text = "Simpan"
        } else { // Mode.DELETE
            binding.tvTitle.text = "Hapus dari Favorit"
            binding.tilNote.visibility = View.GONE
            binding.btnConfirm.text = "Hapus"
            // Ganti warna tombol hapus menjadi merah (error color)
            binding.btnConfirm.setBackgroundColor(requireContext().getColor(R.color.red))
        }
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener {
            if (mode == Mode.ADD) {
                listener?.onSaveClicked(kos, binding.etNote.text.toString().trim())
            } else {
                listener?.onDeleteClicked(kos)
            }
            dismiss()
        }
    }

    fun setFavoriteActionListener(listener: FavoriteActionListener) {
        this.listener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FavoriteActionBottomSheet"
        private const val ARG_KOS = "arg_kos"
        private const val ARG_MODE = "arg_mode"
        private const val ARG_NOTE = "arg_note"

        fun newInstance(kos: Kos, mode: Mode, note: String? = null): FavoriteActionBottomSheet {
            return FavoriteActionBottomSheet().apply {
                arguments = bundleOf(
                    ARG_KOS to kos,
                    ARG_MODE to mode,
                    ARG_NOTE to note
                )
            }
        }
    }
}