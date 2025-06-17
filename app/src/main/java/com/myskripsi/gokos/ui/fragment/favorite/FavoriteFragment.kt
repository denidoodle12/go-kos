// File: ui/fragment/favorite/FavoriteFragment.kt
package com.myskripsi.gokos.ui.fragment.favorite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.myskripsi.gokos.data.model.Favorite
import com.myskripsi.gokos.databinding.FragmentFavoriteBinding
import com.myskripsi.gokos.ui.activity.detailKos.DetailKosActivity
import com.myskripsi.gokos.ui.adapter.FavoriteAdapter
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteFragment : Fragment(), EditNoteBottomSheet.EditNoteListener {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoriteViewModel by viewModel()
    private lateinit var favoriteAdapter: FavoriteAdapter

    private var itemToEdit: Favorite? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserFavorites()
    }

    override fun onNoteSaved(newNote: String) {
        itemToEdit?.let {
            viewModel.updateFavoriteNote(it.id, newNote)
            // Tampilkan Toast atau feedback lain jika perlu
            Toast.makeText(requireContext(), "Catatan diperbarui!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteAdapter()
        binding.rvFavorites.apply { // <-- Menggunakan ID yang konsisten: rv_favorites
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoriteAdapter
        }

        favoriteAdapter.onItemClick = { favoriteItem ->
            val intent = Intent(requireActivity(), DetailKosActivity::class.java).apply {
                putExtra(DetailKosActivity.EXTRA_DETAIL_KOS, favoriteItem.kos)
            }
            startActivity(intent)
        }

        favoriteAdapter.onRemoveClick = { favoriteItem ->
            showRemoveConfirmationDialog(favoriteItem.favorite)
        }

        favoriteAdapter.onNoteClick = { favoriteItem ->
            // Simpan item yang akan diedit
            this.itemToEdit = favoriteItem.favorite
            // Tampilkan bottom sheet
            val bottomSheet = EditNoteBottomSheet.newInstance(favoriteItem.favorite.note)
            bottomSheet.setEditNoteListener(this) // Set fragment ini sebagai listener
            bottomSheet.show(parentFragmentManager, EditNoteBottomSheet.TAG)
        }

    }

    private fun observeViewModel() {
        viewModel.favoritesState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvFavorites.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (result.data.isEmpty()) {
                        binding.rvFavorites.visibility = View.GONE
                        binding.tvError.visibility = View.VISIBLE
                    } else {
                        binding.rvFavorites.visibility = View.VISIBLE
                        binding.tvError.visibility = View.GONE
                        favoriteAdapter.submitList(result.data)
                    }
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvFavorites.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }

        viewModel.removeResult.observe(viewLifecycleOwner) { result ->
            if (result is Result.Success) {
                Toast.makeText(requireContext(), result.data, Toast.LENGTH_SHORT).show()
            } else if (result is Result.Error) {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showRemoveConfirmationDialog(favorite: Favorite) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Favorit")
            .setMessage("Anda yakin ingin menghapus kos ini dari daftar favorit?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.removeFavorite(favorite)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}