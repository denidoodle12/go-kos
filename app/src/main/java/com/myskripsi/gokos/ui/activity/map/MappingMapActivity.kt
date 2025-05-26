package com.myskripsi.gokos.ui.activity.map

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.myskripsi.gokos.R
import com.myskripsi.gokos.databinding.ActivityMappingMapBinding
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

class MappingMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMappingMapBinding
    private val viewModel: MappingMapViewModel by viewModel()

    private var campusId: String? = null
    private var campusName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMappingMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        campusId = intent.getStringExtra(EXTRA_CAMPUS_ID)
        campusName = intent.getStringExtra(EXTRA_CAMPUS_NAME)

        setSupportActionBar(binding.toolbarMappingMap)
        supportActionBar?.title = "Mapping Map ${campusName ?: "campus"}"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (campusId == null) {
            Toast.makeText(this, "Campus ID didn't valid.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        observeViewModel()
        viewModel.loadCampusAndKosDetails(campusId!!)
    }

    companion object {
        const val EXTRA_CAMPUS_ID = "extra_campus_id"
        const val EXTRA_CAMPUS_NAME = "extra_campus_name"
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
    }

    private fun observeViewModel() {
        viewModel.mapDataState.observe(this) { result->
            when(result) {
                is Result.Loading -> {
                    binding.mapProgressBar.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    binding.mapProgressBar.visibility = View.GONE
                    val mapData = result.data
                    displayDataOnMap(mapData)
                }
                is Result.Error -> {
                    binding.mapProgressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed to load data map: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun displayDataOnMap(mapData: MapData) {
        if (!::mMap.isInitialized) return

        mMap.clear()

        val campus = mapData.campus
        val kosList = mapData.kosList

        val campusLatLng = LatLng(campus.lokasi.latitude, campus.lokasi.longitude)
        mMap.addMarker(
            MarkerOptions()
                .position(campusLatLng)
                .title(campus.nama_kampus)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) // Warna beda untuk kampus
        )

        val boundsBuilder = LatLngBounds.Builder()
        boundsBuilder.include(campusLatLng)

        kosList.forEach { kos ->
            if (kos.lokasi.latitude != 0.0 && kos.lokasi.longitude != 0.0) {
                val kosLatLng = LatLng(kos.lokasi.latitude, kos.lokasi.longitude)
                mMap.addMarker(
                    MarkerOptions()
                        .position(kosLatLng)
                        .title(kos.nama_kost)
                        .snippet("Price: Rp. ${kos.harga}")
                )
                boundsBuilder.include(kosLatLng)
            }
        }

        if (kosList.isEmpty()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusLatLng, 15f))
        } else {
            val bounds = boundsBuilder.build()
            val padding = 100
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            mMap.animateCamera(cameraUpdate)
        }
    }
}