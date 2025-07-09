package com.myskripsi.gokos.ui.activity.search

import android.Manifest
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myskripsi.gokos.R
import com.myskripsi.gokos.databinding.ActivitySearchBinding
import com.myskripsi.gokos.ui.activity.detailKos.DetailKosActivity
import com.myskripsi.gokos.ui.activity.listkos.ListKosActivity
import com.myskripsi.gokos.ui.adapter.CampusAdapter
import com.myskripsi.gokos.ui.adapter.KosAdapter
import com.myskripsi.gokos.utils.LocationHelper
import com.myskripsi.gokos.utils.LocationResult
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModel()

    private lateinit var campusAdapter: CampusAdapter
    private lateinit var searchResultsAdapter: KosAdapter

    // Komponen untuk menangani lokasi
    private lateinit var locationHelper: LocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi LocationHelper
        locationHelper = LocationHelper(this)

        setupToolbar()
        setupRecyclerViews()
        setupSearchView()
        observeViewModel()

        // Memulai proses mendapatkan lokasi saat activity dibuat
        initiateLocationProcess()
    }

    override fun onResume() {
        super.onResume()
        // Cek ulang izin dan lokasi jika pengguna kembali ke aplikasi dari pengaturan
        if (locationHelper.hasLocationPermission()) {
            fetchCurrentUserLocation()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerViews() {
        // Adapter untuk opsi kampus (horizontal)
        campusAdapter = CampusAdapter()
        binding.rvCampusOptions.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity, RecyclerView.HORIZONTAL, false)
            adapter = campusAdapter
        }
        campusAdapter.onItemClick = { campus ->
            val intent = Intent(this, ListKosActivity::class.java).apply {
                putExtra(ListKosActivity.EXTRA_CAMPUS_ID, campus.id)
            }
            startActivity(intent)
        }

        // Adapter untuk hasil pencarian kos (vertikal)
        searchResultsAdapter = KosAdapter()
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchResultsAdapter
        }
        searchResultsAdapter.onItemClick = { kos ->
            val intent = Intent(this, DetailKosActivity::class.java).apply {
                putExtra(DetailKosActivity.EXTRA_DETAIL_KOS, kos)
            }
            startActivity(intent)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty()
                if (query.isBlank()) {
                    // Jika query kosong, tampilkan opsi kampus dan bersihkan hasil pencarian
                    binding.rvCampusOptions.visibility = View.VISIBLE
                    binding.rvSearchResults.visibility = View.GONE
                    viewModel.performSearch("") // Kirim query kosong untuk clear list
                } else {
                    // Jika ada query, tampilkan hasil pencarian
                    binding.rvCampusOptions.visibility = View.GONE
                    binding.rvSearchResults.visibility = View.VISIBLE
                    viewModel.performSearch(query)
                }
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.campusListState.observe(this) { result ->
            if (result is Result.Success) {
                campusAdapter.submitList(result.data)
            }
        }

        viewModel.searchResultsState.observe(this) { kosList ->
            searchResultsAdapter.submitList(kosList)
        }
    }

    // --- LOGIKA LOKASI YANG LENGKAP ---

    private fun initiateLocationProcess() {
        if (locationHelper.hasLocationPermission()) {
            fetchCurrentUserLocation()
        } else {
            // Minta izin jika belum diberikan
            locationHelper.requestLocationPermissions { permissions ->
                handlePermissionResult(permissions)
            }
        }
    }

    private fun fetchCurrentUserLocation() {
        lifecycleScope.launch {
            when (val locationResult = locationHelper.getCurrentLocation()) {
                is LocationResult.Success -> {
                    // Jika lokasi berhasil didapat, update ViewModel
                    viewModel.updateUserLocation(locationResult.location)
                }
                else -> {
                    // Jika gagal (misal: GPS mati), tetap update ViewModel dengan null
                    viewModel.updateUserLocation(null)
                    Toast.makeText(this@SearchActivity, "Gagal mendapatkan lokasi, jarak mungkin tidak akurat.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            // Jika izin diberikan, ambil lokasi
            fetchCurrentUserLocation()
        } else {
            // Jika izin ditolak
            Toast.makeText(this, "Izin lokasi ditolak, jarak tidak akan ditampilkan.", Toast.LENGTH_LONG).show()
            // Cek jika pengguna memilih "Don't ask again"
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showSettingsDialog()
            }
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Izin Diperlukan")
            .setMessage("Aplikasi ini memerlukan izin lokasi untuk menampilkan jarak. Aktifkan di Pengaturan Aplikasi.")
            .setPositiveButton("Pengaturan") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .create()
            .show()
    }
}