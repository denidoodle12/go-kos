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
import androidx.recyclerview.widget.LinearLayoutManager
import com.myskripsi.gokos.databinding.FragmentHomeBinding
import com.myskripsi.gokos.ui.activity.listkos.ListKosActivity
import com.myskripsi.gokos.ui.activity.detailKos.DetailKosActivity
import com.myskripsi.gokos.ui.adapter.KosAdapter
import com.myskripsi.gokos.utils.LocationHelper
import com.myskripsi.gokos.utils.LocationResult
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModel()
    private lateinit var nearbyKosAdapter: KosAdapter
    private lateinit var locationHelper: LocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initiate LocationHelper in onCreate
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

        setupCampusNavigationClickListeners()
        setupNearbyKosRecyclerView()
        observeViewModel()

        binding.progressIndicator.visibility = View.GONE
        binding.tvNearbyKosStatus.visibility = View.GONE
        binding.rvNearbyKos.visibility = View.GONE

        initiateLocationProcess()
    }

    private fun setupCampusNavigationClickListeners() {
        binding.cardViewUnsera.setOnClickListener {
            navigateToCampus("Pl7iiRGC0FbENz4bnHwq")
        }
        binding.cardViewUniba.setOnClickListener {
            navigateToCampus("kc9H58uqdcNcldwF2ce8")
        }
        binding.cardViewUIN.setOnClickListener {
            navigateToCampus("BnNlYEeXsxnCva26gIwu")
        }
    }

    private fun navigateToCampus(campusId: String) {
        val intent = Intent(requireContext(), ListKosActivity::class.java).apply {
            putExtra(ListKosActivity.EXTRA_CAMPUS_ID, campusId)
        }
        startActivity(intent)
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
            setHasFixedSize(true)
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
                    binding.progressIndicator.visibility = View.GONE
                    if (result.data.isEmpty()) {
                        binding.tvNearbyKosStatus.text = "Tidak ada kos ditemukan di sekitarmu."
                        binding.tvNearbyKosStatus.visibility = View.VISIBLE
                        binding.rvNearbyKos.visibility = View.GONE
                    } else {
                        binding.tvNearbyKosStatus.visibility = View.GONE
                        binding.rvNearbyKos.visibility = View.VISIBLE
                        nearbyKosAdapter.submitList(result.data)
                    }
                }
                is Result.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    binding.rvNearbyKos.visibility = View.GONE
                    binding.tvNearbyKosStatus.text = result.message
                    binding.tvNearbyKosStatus.visibility = View.VISIBLE
                }
            }
        }

        viewModel.userLocation.observe(viewLifecycleOwner) { location ->
            if (location == null && viewModel.nearbyKosState.value !is Result.Success) {
                if (binding.progressIndicator.visibility == View.GONE && binding.rvNearbyKos.visibility == View.GONE) {
                    binding.tvNearbyKosStatus.text = "Gagal mendapatkan lokasi Anda untuk mencari kos terdekat."
                    binding.tvNearbyKosStatus.visibility = View.VISIBLE
                }
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
                    binding.tvNearbyKosStatus.text = "Layanan lokasi (GPS) tidak aktif. Mohon aktifkan."
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
            .setTitle("Izin Lokasi Dibutuhkan")
            .setMessage("Aplikasi ini membutuhkan izin lokasi untuk menampilkan kos terdekat di sekitar Anda.")
            .setPositiveButton("Berikan Izin") { _, _ ->
                locationHelper.requestLocationPermissions { permissions ->
                    handlePermissionResult(permissions)
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
                handlePermissionDeniedUI(showSettingsOption = false)
            }
            .create()
            .show()
    }

    private fun handlePermissionDeniedUI(showSettingsOption: Boolean) {
        binding.tvNearbyKosStatus.text = "Izin lokasi ditolak. Fitur kos terdekat tidak dapat digunakan."
        binding.tvNearbyKosStatus.visibility = View.VISIBLE
        binding.progressIndicator.visibility = View.GONE
        binding.rvNearbyKos.visibility = View.GONE
        Toast.makeText(requireContext(), "Izin lokasi ditolak.", Toast.LENGTH_SHORT).show()

        if (showSettingsOption) {
            showSettingsDialog()
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Izin Diperlukan")
            .setMessage("Aplikasi ini memerlukan izin lokasi. Aktifkan di Pengaturan Aplikasi.")
            .setPositiveButton("Pengaturan") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Batal") { dialog, _ ->
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
                binding.tvNearbyKosStatus.text = "Berikan izin lokasi untuk melihat kos terdekat."
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
