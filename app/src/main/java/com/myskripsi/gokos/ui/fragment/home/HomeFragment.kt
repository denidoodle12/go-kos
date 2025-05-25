package com.myskripsi.gokos.ui.fragment.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.myskripsi.gokos.utils.Result
import com.myskripsi.gokos.databinding.FragmentHomeBinding // Pastikan ini benar
import com.myskripsi.gokos.ui.activity.ListKosActivity
import com.myskripsi.gokos.ui.activity.detailKos.DetailKosActivity
import com.myskripsi.gokos.ui.adapter.KosAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModel()
    private lateinit var nearbyKosAdapter: KosAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var requestingLocationUpdates = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            when {
                fineLocationGranted -> getCurrentLocation()
                coarseLocationGranted -> getCurrentLocation()
                else -> handleLocationPermissionDenied()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCampusNavigationClickListeners() // Menggunakan CardView dari layout Anda
        setupNearbyKosRecyclerView()
        observeViewModel()

        // Inisialisasi state UI awal untuk kos terdekat
        binding.progressIndicator.visibility = View.GONE
        binding.tvNearbyKosStatus.visibility = View.GONE // Menggunakan ID baru
        binding.rvNearbyKos.visibility = View.GONE

        checkAndRequestLocationPermission()
    }

    private fun setupCampusNavigationClickListeners() {
        binding.cardViewUnsera.setOnClickListener {
            navigateToCampus("Pl7iiRGC0FbENz4bnHwq")
        }
        binding.cardViewUniba.setOnClickListener {
            navigateToCampus("kc9H58uqdcNcldwF2ce8") // Ganti dengan ID Uniba yang benar
        }
        binding.cardViewUIN.setOnClickListener {
            navigateToCampus("BnNlYEeXsxnCva26gIwu")
        }
        // Anda mungkin punya card view lain untuk FKIP Untirta, tambahkan di sini jika ada
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
            // Layout Manager HORIZONTAL sesuai XML Anda
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = nearbyKosAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.nearbyKosState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE // Menggunakan progressIndicator dari XML
                    binding.rvNearbyKos.visibility = View.GONE
                    binding.tvNearbyKosStatus.visibility = View.GONE // Menggunakan ID baru
                }
                is Result.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    if (result.data.isEmpty()) {
                        binding.tvNearbyKosStatus.text = "Tidak ada kos ditemukan di sekitarmu." // Menggunakan ID baru
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
                    binding.tvNearbyKosStatus.text = result.message // Menggunakan ID baru
                    binding.tvNearbyKosStatus.visibility = View.VISIBLE
                }
            }
        }

        viewModel.userLocation.observe(viewLifecycleOwner) { location ->
            if (location == null && !requestingLocationUpdates && viewModel.nearbyKosState.value !is Result.Success) {
                // Hanya tampilkan error jika lokasi null, tidak sedang request, dan belum ada data sukses
                binding.tvNearbyKosStatus.text = "Gagal mendapatkan lokasi Anda. Pastikan GPS aktif dan izin diberikan."
                binding.tvNearbyKosStatus.visibility = View.VISIBLE
                binding.progressIndicator.visibility = View.GONE
                binding.rvNearbyKos.visibility = View.GONE
            }
        }
    }

    private fun checkAndRequestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionRationaleDialog()
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Izin Lokasi Dibutuhkan")
            .setMessage("Aplikasi ini membutuhkan izin lokasi untuk menampilkan kos terdekat di sekitar Anda.")
            .setPositiveButton("Berikan Izin") { _, _ ->
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
                handleLocationPermissionDenied()
            }
            .create() // tambahkan create()
            .show()
    }

    private fun handleLocationPermissionDenied(showSettingsOption: Boolean = true) {
        binding.tvNearbyKosStatus.text = "Izin lokasi ditolak. Fitur kos terdekat tidak dapat digunakan."
        binding.tvNearbyKosStatus.visibility = View.VISIBLE
        binding.progressIndicator.visibility = View.GONE
        binding.rvNearbyKos.visibility = View.GONE
        Toast.makeText(requireContext(), "Izin lokasi ditolak.", Toast.LENGTH_LONG).show()

        if (showSettingsOption && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Pengguna memilih "Don't ask again" atau izin ditolak permanen
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
            .create() // tambahkan create()
            .show()
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        binding.progressIndicator.visibility = View.VISIBLE
        binding.tvNearbyKosStatus.visibility = View.GONE
        binding.rvNearbyKos.visibility = View.GONE

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null && _binding != null) { // Cek binding juga
                    viewModel.updateUserLocation(location)
                    stopLocationUpdates()
                } else if (_binding != null) { // Cek binding
                    requestNewLocationData()
                }
            }
            .addOnFailureListener {
                if (_binding != null) { // Cek binding
                    requestNewLocationData()
                }
            }
    }

    private fun requestNewLocationData() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (!isAdded || _binding == null) return // Pastikan fragment masih ter-attach dan binding ada

        requestingLocationUpdates = true // Set flag sebelum request

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .setMaxUpdates(1)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (!isAdded || _binding == null) { // Cek lagi sebelum proses
                    stopLocationUpdates() // Hentikan jika fragment sudah detached
                    return
                }
                val lastLocation: Location? = locationResult.lastLocation
                viewModel.updateUserLocation(lastLocation) // Bisa null jika gagal
                stopLocationUpdates()
            }

            // Tambahkan onLocationAvailability untuk menangani jika lokasi tidak tersedia
            override fun onLocationAvailability(locationAvailability: com.google.android.gms.location.LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                if (!locationAvailability.isLocationAvailable && requestingLocationUpdates) {
                    if (!isAdded || _binding == null) {
                        stopLocationUpdates()
                        return
                    }
                    viewModel.updateUserLocation(null) // Kirim null jika lokasi tidak tersedia
                    stopLocationUpdates()
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.myLooper())
    }

    private fun stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback!!)
            locationCallback = null
        }
        requestingLocationUpdates = false // Reset flag
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (viewModel.userLocation.value == null && !requestingLocationUpdates) {
                getCurrentLocation()
            }
        } else {
            // Jika izin belum ada, state UI error untuk kos terdekat bisa ditampilkan di sini
            // atau biarkan checkAndRequestLocationPermission() yang mengaturnya saat onViewCreated.
            if(viewModel.nearbyKosState.value !is Result.Success){ // Hanya jika belum ada data sukses
                binding.tvNearbyKosStatus.text = "Berikan izin lokasi untuk melihat kos terdekat."
                binding.tvNearbyKosStatus.visibility = View.VISIBLE
                binding.progressIndicator.visibility = View.GONE
                binding.rvNearbyKos.visibility = View.GONE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationCallback = null
        _binding = null
    }
}