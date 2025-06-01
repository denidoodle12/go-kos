package com.myskripsi.gokos.ui.fragment.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.myskripsi.gokos.R
import com.myskripsi.gokos.databinding.FragmentHomeBinding
import com.myskripsi.gokos.ui.activity.listkos.ListKosActivity
import com.myskripsi.gokos.ui.activity.detailKos.DetailKosActivity
import com.myskripsi.gokos.ui.adapter.CampusAdapter
import com.myskripsi.gokos.ui.adapter.KosAdapter
import com.myskripsi.gokos.utils.LocationHelper
import com.myskripsi.gokos.utils.LocationResult
import kotlinx.coroutines.launch
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModel()
    private lateinit var nearbyKosAdapter: KosAdapter
    private lateinit var campusAdapter: CampusAdapter
    private lateinit var locationHelper: LocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationHelper = LocationHelper(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNearbyKosRecyclerView()
        setupCampusRecyclerView()
        observeViewModel()

        initiateLocationProcess()
        viewModel.fetchCampusList()
        viewModel.loadUserProfile()
    }

    private fun setupNearbyKosRecyclerView() {
        nearbyKosAdapter = KosAdapter()
        nearbyKosAdapter.onItemClick = { selectedKos ->
            val intent = Intent(requireContext(), DetailKosActivity::class.java).apply {
                putExtra(DetailKosActivity.EXTRA_DETAIL_KOS, selectedKos)
            }
            startActivity(intent)
        }
        binding.rvNearbyKos.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = nearbyKosAdapter
        }
    }

    private fun setupCampusRecyclerView() {
        campusAdapter = CampusAdapter()
        campusAdapter.onItemClick = { campus ->
            val intent = Intent(requireContext(), ListKosActivity::class.java).apply {
                putExtra(ListKosActivity.EXTRA_CAMPUS_ID, campus.id)
                putExtra(ListKosActivity.EXTRA_CAMPUS_NAME, campus.nama_kampus)
            }
            startActivity(intent)
        }
        binding.rvCampusSelection.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = campusAdapter
        }
    }


    private fun observeViewModel() {
        viewModel.nearbyKosState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.rvNearbyKos.visibility = View.GONE
                    binding.tvNearbyKosStatus.visibility = View.GONE
                }
                is Result.Success -> {
                    if (viewModel.campusListState.value !is Result.Loading) {
                        binding.progressIndicator.visibility = View.GONE
                    }
                    if (result.data.isEmpty()) {
                        binding.tvNearbyKosStatus.text = getString(R.string.txt_cannot_find_kos_nearby_you)
                        binding.tvNearbyKosStatus.visibility = View.VISIBLE
                        binding.rvNearbyKos.visibility = View.GONE
                    } else {
                        binding.tvNearbyKosStatus.visibility = View.GONE
                        binding.rvNearbyKos.visibility = View.VISIBLE
                        nearbyKosAdapter.submitList(result.data)
                    }
                }
                is Result.Error -> {
                    if (viewModel.campusListState.value !is Result.Loading) {
                        binding.progressIndicator.visibility = View.GONE
                    }
                    binding.rvNearbyKos.visibility = View.GONE
                    binding.tvNearbyKosStatus.text = result.message
                    binding.tvNearbyKosStatus.visibility = View.VISIBLE
                }
            }
        }

        viewModel.campusListState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.rvCampusSelection.visibility = View.GONE
                    binding.tvCampusListError.visibility = View.GONE
                }
                is Result.Success -> {
                    if (viewModel.nearbyKosState.value !is Result.Loading) {
                        binding.progressIndicator.visibility = View.GONE
                    }
                    if (result.data.isEmpty()) {
                        binding.tvCampusListError.text = getString(R.string.txt_campus_list_not_available)
                        binding.tvCampusListError.visibility = View.VISIBLE
                        binding.rvCampusSelection.visibility = View.GONE
                    } else {
                        binding.tvCampusListError.visibility = View.GONE
                        binding.rvCampusSelection.visibility = View.VISIBLE
                        campusAdapter.submitList(result.data)
                    }
                }
                is Result.Error -> {
                    if (viewModel.nearbyKosState.value !is Result.Loading) {
                        binding.progressIndicator.visibility = View.GONE
                    }
                    binding.rvCampusSelection.visibility = View.GONE
                    binding.tvCampusListError.text = result.message
                    binding.tvCampusListError.visibility = View.VISIBLE
                }
            }
        }

        viewModel.userLocation.observe(viewLifecycleOwner) { location ->
            if (location == null &&
                viewModel.nearbyKosState.value !is Result.Success &&
                binding.progressIndicator.visibility == View.GONE &&
                binding.rvNearbyKos.visibility == View.GONE) {

                binding.tvNearbyKosStatus.text = getString(R.string.txt_unable_to_get_your_location_to_find_the_nearby_kos)
                binding.tvNearbyKosStatus.visibility = View.VISIBLE
            }
        }

        viewModel.userDisplayName.observe(viewLifecycleOwner) { displayName ->
            if (!displayName.isNullOrBlank()) {
                binding.username.text = getString(R.string.hiUsername, displayName)
            } else {
                binding.username.text = getString(R.string.hiUsername, "User")
            }
        }
    }

    private fun initiateLocationProcess() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (locationHelper.hasLocationPermission()) {
                fetchLocationAndNotifyViewModel()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showPermissionRationaleDialog()
                } else {
                    locationHelper.requestLocationPermissions { permissions ->
                        handlePermissionResult(permissions)
                    }
                }
            }
        }
    }

    private fun fetchLocationAndNotifyViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressIndicator.visibility = View.VISIBLE
            binding.tvNearbyKosStatus.visibility = View.GONE
            binding.rvNearbyKos.visibility = View.GONE

            when (val helperResult = locationHelper.getCurrentLocation()) {
                is LocationResult.Success -> {
                    viewModel.updateUserLocation(helperResult.location)
                }
                is LocationResult.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    binding.tvNearbyKosStatus.text = helperResult.message
                    binding.tvNearbyKosStatus.visibility = View.VISIBLE
                    viewModel.updateUserLocation(null)
                }
                is LocationResult.PermissionDenied -> {
                    binding.progressIndicator.visibility = View.GONE
                    handlePermissionDeniedUI(showSettingsOption = !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                }
                is LocationResult.LocationDisabled -> {
                    binding.progressIndicator.visibility = View.GONE
                    binding.tvNearbyKosStatus.text = getString(R.string.txt_location_services_gps_are_disabled_please_enable_them)
                    binding.tvNearbyKosStatus.visibility = View.VISIBLE
                    viewModel.updateUserLocation(null)
                }
                is LocationResult.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            fetchLocationAndNotifyViewModel()
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                handlePermissionDeniedUI(showSettingsOption = true)
            } else {
                handlePermissionDeniedUI(showSettingsOption = false)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Needed!")
            .setMessage("This app requires location permission to display kos nearby you.")
            .setPositiveButton("Grant Permission") { _, _ ->
                locationHelper.requestLocationPermissions { permissions ->
                    handlePermissionResult(permissions)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                handlePermissionDeniedUI(showSettingsOption = false)
            }
            .create()
            .show()
    }

    private fun handlePermissionDeniedUI(showSettingsOption: Boolean) {
        binding.tvNearbyKosStatus.text = getString(R.string.txt_location_permission_denied_nearby_features_cannot_be_used)
        binding.tvNearbyKosStatus.visibility = View.VISIBLE
        binding.progressIndicator.visibility = View.GONE //
        binding.rvNearbyKos.visibility = View.GONE
        Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT).show()

        if (showSettingsOption) {
            showSettingsDialog()
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Needed!")
            .setMessage("This app requires location permission. Enable it in App Settings.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (locationHelper.hasLocationPermission()) {
            if (viewModel.userLocation.value == null || viewModel.nearbyKosState.value is Result.Error) {
                if (viewModel.nearbyKosState.value !is Result.Success || viewModel.userLocation.value == null) {
                    fetchLocationAndNotifyViewModel()
                }
            }
        } else {
            if (viewModel.nearbyKosState.value !is Result.Success) {
                binding.tvNearbyKosStatus.text =
                    getString(R.string.txt_allow_location_access_to_see_kos_nearby_you)
                binding.tvNearbyKosStatus.visibility = View.VISIBLE
                binding.progressIndicator.visibility = View.GONE
                binding.rvNearbyKos.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}