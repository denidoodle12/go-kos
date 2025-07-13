package com.myskripsi.gokos.ui.fragment.favorite

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.Favorite
import com.myskripsi.gokos.databinding.FragmentFavoriteBinding
import com.myskripsi.gokos.ui.activity.detailKos.DetailKosActivity
import com.myskripsi.gokos.ui.adapter.FavoriteAdapter
import com.myskripsi.gokos.utils.LocationHelper
import com.myskripsi.gokos.utils.LocationResult
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteFragment : Fragment(), EditNoteBottomSheet.EditNoteListener {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoriteViewModel by viewModel()
    private lateinit var favoriteAdapter: FavoriteAdapter
    private var itemToEdit: Favorite? = null

    private lateinit var locationHelper: LocationHelper

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            locationHelper.handlePermissionResult(permissions)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationHelper = LocationHelper(requireActivity() as AppCompatActivity, requestPermissionLauncher)
    }

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
        initiateLocationProcess()
    }

    private fun initiateLocationProcess() {
        if (locationHelper.hasLocationPermission()) {
            fetchLocationAndLoadFavorites()
        } else {
            locationHelper.requestLocationPermissions { permissions ->
                if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
                    fetchLocationAndLoadFavorites()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
                    viewModel.loadUserFavorites()
                }
            }
        }
    }

    private fun fetchLocationAndLoadFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            val locationResult = locationHelper.getCurrentLocation()
            if (locationResult is LocationResult.Success) {
                viewModel.loadUserFavorites(locationResult.location)
            } else {
                viewModel.loadUserFavorites()
            }
        }
    }

    override fun onNoteSaved(newNote: String) {
        itemToEdit?.let {
            viewModel.updateFavoriteNote(it.id, newNote)
            Toast.makeText(requireContext(), getString(R.string.note_updated), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteAdapter()
        binding.rvFavorites.apply {
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
            this.itemToEdit = favoriteItem.favorite
            val bottomSheet = EditNoteBottomSheet.newInstance(favoriteItem.favorite.note)
            bottomSheet.setEditNoteListener(this)
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
            .setTitle(getString(R.string.delete_favorit))
            .setMessage(getString(R.string.delete_confirmation))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.removeFavorite(favorite)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}