package com.myskripsi.gokos.ui.activity.search

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    private lateinit var locationHelper: LocationHelper

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            locationHelper.handlePermissionResult(permissions)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationHelper = LocationHelper(this, requestPermissionLauncher)

        setupToolbar()
        setupRecyclerViews()
        setupSearchView()
        observeViewModel()
        initiateLocationProcess()
    }

    override fun onResume() {
        super.onResume()
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
                    binding.rvCampusOptions.visibility = View.VISIBLE
                    binding.rvSearchResults.visibility = View.GONE
                    viewModel.performSearch("")
                } else {
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

    private fun initiateLocationProcess() {
        if (locationHelper.hasLocationPermission()) {
            fetchCurrentUserLocation()
        } else {
            locationHelper.requestLocationPermissions { permissions ->
                handlePermissionResult(permissions)
            }
        }
    }

    private fun fetchCurrentUserLocation() {
        lifecycleScope.launch {
            when (val locationResult = locationHelper.getCurrentLocation()) {
                is LocationResult.Success -> {
                    viewModel.updateUserLocation(locationResult.location)
                }
                else -> {
                    viewModel.updateUserLocation(null)
                    Toast.makeText(this@SearchActivity,
                        getString(R.string.failed_to_obtain_location_distance), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            fetchCurrentUserLocation()
        } else {
            Toast.makeText(this, getString(R.string.permit_location_denied), Toast.LENGTH_LONG).show()
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showSettingsDialog()
            }
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.location_permission_needed))
            .setMessage(getString(R.string.this_app_requires_location_permission_to_display_kos_nearby_you))
            .setPositiveButton(getString(R.string.grant_permission)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
            .show()
    }
}