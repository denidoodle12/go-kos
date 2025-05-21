package com.myskripsi.gokos.ui.fragment.home

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.myskripsi.gokos.databinding.FragmentHomeBinding
import com.myskripsi.gokos.ui.activity.ListKosActivity
import com.myskripsi.gokos.ui.activity.detailKos.DetailKosActivity
import com.myskripsi.gokos.ui.adapter.KosAdapter
import com.myskripsi.gokos.utils.LocationHelper
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModel()
    private lateinit var locationHelper: LocationHelper
    private lateinit var nearbyKosAdapter: KosAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isLocationGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isLocationGranted) {
            getUserLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Location permission required to show nearby kos data",
                Toast.LENGTH_LONG
            ).show()
        }

        // Use default location if permission denied
        useDefaultLocation()
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

        locationHelper = LocationHelper(requireContext())
        showDataNearbyKos()
        navigateToListKosActivity()
        checkLocationPermission()
    }

    private fun showDataNearbyKos() {
        nearbyKosAdapter = KosAdapter()
        nearbyKosAdapter.onItemClick = { selectedData ->
            val intent = Intent(requireContext(), DetailKosActivity::class.java).apply {
                putExtra(DetailKosActivity.EXTRA_DETAIL_KOS, selectedData)
            }
            startActivity(intent)
        }

        viewModel.nearbyKos.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    val nearbyKos = result.data
                    if (nearbyKos.isEmpty()) {
                        Log.d("empty","nearby kos is empty")
                    } else {
                        nearbyKosAdapter.submitList(nearbyKos.take(5))
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    Log.e("HomeFragment", "Error getting nearby kos: ${result.message}")
                }
            }

            with(binding.rvNearbyKos) {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                setHasFixedSize(true)
                adapter = nearbyKosAdapter
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            locationHelper.hasLocationPermission() -> {
                getUserLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(
                    requireContext(),
                    "GoKos need location permission to show nearby kos from your place",
                    Toast.LENGTH_LONG
                ).show()
                requestLocationPermission()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun getUserLocation() {
        // Tampilkan progress bar saat memuat
        binding.progressIndicator.visibility = View.VISIBLE

        // Menggunakan coroutine untuk mendapatkan lokasi
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val location = locationHelper.getCurrentLocationSuspend()

                if (location != null) {
                    // Dapatkan kos terdekat dari lokasi pengguna
                    viewModel.getNearbyKos(location.latitude, location.longitude)
                    Log.d("HomeFragment", "Lokasi pengguna: ${location.latitude}, ${location.longitude}")
                } else {
                    // Gunakan lokasi default jika lokasi tidak tersedia
                    useDefaultLocation()
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error saat mendapatkan lokasi: ${e.message}")
                useDefaultLocation()
            }
        }
    }

    private fun useDefaultLocation() {
        val defaultLat = -6.0880477
        val defaultLng = 106.1322941

        viewModel.getNearbyKos(defaultLat, defaultLng)

        Toast.makeText(
            requireContext(),
            "Tidak dapat menentukan lokasi Anda, menggunakan lokasi Universitas Serang Raya",
            Toast.LENGTH_SHORT
        ).show()

        Log.d("HomeFragment", "Menggunakan lokasi default: $defaultLat, $defaultLng")
    }

    private fun navigateToListKosActivity() {
        binding.cardViewUnsera.setOnClickListener {
            val intent = Intent(requireContext(), ListKosActivity::class.java).apply {
                putExtra(ListKosActivity.EXTRA_CAMPUS_ID, "Pl7iiRGC0FbENz4bnHwq")
            }
            startActivity(intent)
        }
        binding.cardViewUniba.setOnClickListener {
            val intent = Intent(requireContext(), ListKosActivity::class.java).apply {
                putExtra(ListKosActivity.EXTRA_CAMPUS_ID, "kc9H58uqdcNcldwF2ce8")
            }
            startActivity(intent)
        }
        binding.cardViewUIN.setOnClickListener {
            val intent = Intent(requireContext(), ListKosActivity::class.java).apply {
                putExtra(ListKosActivity.EXTRA_CAMPUS_ID, "BnNlYEeXsxnCva26gIwu")
            }
            startActivity(intent)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvNearbyKos.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

        override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}