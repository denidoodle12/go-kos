package com.myskripsi.gokos.ui.activity.map

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.Campus
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.databinding.ActivityMappingMapBinding
import com.myskripsi.gokos.databinding.LayoutMarkerKosPanelBinding
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class MappingMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMappingMapBinding
    private lateinit var panelBinding: LayoutMarkerKosPanelBinding

    private val viewModel: MappingMapViewModel by viewModel()

    private var campusId: String? = null
    private var campusName: String? = null
    private var currentCampusData: Campus? = null
    private var currentShownKos: Kos? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMappingMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        panelBinding = LayoutMarkerKosPanelBinding.bind(binding.kosDetailPanel.root)

        campusId = intent.getStringExtra(EXTRA_CAMPUS_ID)
        campusName = intent.getStringExtra(EXTRA_CAMPUS_NAME)

        if (campusId == null) {
            Toast.makeText(this, "Campus ID didn't valid.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupPanelListeners()
        observeViewModel()
        viewModel.loadCampusAndKosDetails(campusId!!)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener { hideKosDetailPanel() }
    }

    private fun setupPanelListeners() {
        panelBinding.ibPanelClose.setOnClickListener {
            hideKosDetailPanel()
        }
        panelBinding.fabPanelDirections.setOnClickListener {
            currentShownKos?.let { kos ->
                initiateDirectionsToKos(kos)
            }
        }
    }

    private fun initiateDirectionsToKos(kos: Kos) {
        val destinationLat = kos.lokasi.latitude
        val destinationLng = kos.lokasi.longitude
        val kosNameLabel = kos.nama_kost

        val gmmIntentUri = Uri.parse("geo:0,0?q=$destinationLat,$destinationLng($kosNameLabel)")

        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            try {
                val webUri = Uri.parse("https://developers.google.com/maps/documentation/android-sdk/infowindows#custom_infowindows")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                startActivity(webIntent)
                Toast.makeText(this, "Google Maps is not installed. Try opening it in your browser.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "No map application or browser can handle this.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.mapDataState.observe(this) { result->
            when(result) {
                is Result.Loading -> {
                    binding.mapProgressBar.visibility = View.VISIBLE
                    hideKosDetailPanel()
                }
                is Result.Success -> {
                    binding.mapProgressBar.visibility = View.GONE
                    val mapData = result.data
                    currentCampusData = mapData.campus // Simpan data kampus saat ini
                    displayDataOnMap(mapData)
                }
                is Result.Error -> {
                    binding.mapProgressBar.visibility = View.GONE
                    hideKosDetailPanel()
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
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )

        val boundsBuilder = LatLngBounds.Builder()
        if (campus.lokasi.latitude != 0.0 || campus.lokasi.longitude != 0.0) {
            boundsBuilder.include(campusLatLng)
        }

        kosList.forEach { kos ->
            if (kos.lokasi.latitude != 0.0 && kos.lokasi.longitude != 0.0) {
                val kosLatLng = LatLng(kos.lokasi.latitude, kos.lokasi.longitude)
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(kosLatLng)
                        .title(kos.nama_kost)
                )
                marker?.tag = kos
                boundsBuilder.include(kosLatLng)
            }
        }

        try {
            val bounds = boundsBuilder.build()

            if (bounds.northeast != bounds.southwest) {
                val padding = resources.displayMetrics.widthPixels / 6 // padding dalam pixels
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            } else if (campus.lokasi.latitude != 0.0 || campus.lokasi.longitude != 0.0) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(campusLatLng, 15f))
            } else {
                Toast.makeText(this, "Location data not found to display on map.", Toast.LENGTH_LONG).show()
            }
        } catch (e: IllegalStateException) {
            if (campus.lokasi.latitude != 0.0 || campus.lokasi.longitude != 0.0) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(campusLatLng, 15f))
            } else {
                Toast.makeText(this, "There is no valid location data to display.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        hideKosDetailPanel()

        val kos = marker.tag as? Kos
        if (kos != null) {
            currentShownKos = kos
            showKosDetailPanel(kos)
            val offset = (binding.kosDetailPanel.root.height * 0.3).toInt()
            val point = mMap.projection.toScreenLocation(marker.position)
            point.y -= offset
            val newLatLng = mMap.projection.fromScreenLocation(point)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(newLatLng), 300, null)

            return true
        }
        return false
    }

    private fun showKosDetailPanel(kos: Kos) {
        panelBinding.tvPanelKosName.text = kos.nama_kost
        panelBinding.tvPanelKosCategory.text = kos.kategori
        panelBinding.tvPanelKosAddress.text = kos.alamat

        val facilitiesBuilder = StringBuilder()
        if (kos.fasilitas_kamar.isNotEmpty()) {
            facilitiesBuilder.append(kos.fasilitas_kamar.joinToString(", "))
        }
        if (kos.fasilitas_kamar_mandi.isNotEmpty()) {
            if (facilitiesBuilder.isNotEmpty()) {
                facilitiesBuilder.append(", ")
            }
            facilitiesBuilder.append(kos.fasilitas_kamar_mandi.joinToString(", "))
        }
        panelBinding.tvPanelKosFacilities.text = if (facilitiesBuilder.isBlank()) "-" else facilitiesBuilder.toString()

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        panelBinding.tvPanelKosPrice.text = "${formatRupiah.format(kos.harga.toDouble())} / bulan"

        if (kos.foto_kost.isNotEmpty()) {
            Glide.with(this)
                .load(kos.foto_kost[0])
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(panelBinding.ivPanelKosImage)
        } else {
            panelBinding.ivPanelKosImage.setImageResource(R.drawable.placeholder_image)
        }

        currentCampusData?.let { campus ->
            val distanceInKm = kos.lokasi.jarak
            panelBinding.tvPanelKosDistance.text = formatDistanceForPanel(distanceInKm, campus.nama_kampus)
        } ?: run {
            panelBinding.tvPanelKosDistance.text = "Info jarak tidak tersedia"
        }

        binding.kosDetailPanel.root.visibility = View.VISIBLE
    }

    private fun formatDistanceForPanel(distanceInKm: Double, campusName: String): String {
        val campusShortName = campusName.replace("Universitas Bina Bangsa ", "Uniba ")
            .replace("Universitas Serang Raya ", "Unsera ")
            .replace("Sekolah Tinggi ", "ST ")
            .replace("Institut ", "Inst. ")

        return if (distanceInKm < 0) {
            "Distance N/A"
        } else if (distanceInKm == 0.0 && campusName.isNotBlank()){
            "📍 Tepat di $campusShortName"
        } else if (distanceInKm < 0.01) {
            val distanceInMeters = distanceInKm * 1000
            "📍 ${DecimalFormat("#0").format(distanceInMeters)} m dari $campusShortName"
        } else if (distanceInKm < 1.0) {
            val distanceInMeters = distanceInKm * 1000
            "📍 ${DecimalFormat("#0").format(distanceInMeters)} m dari $campusShortName"
        } else {
            "📍 ${DecimalFormat("#0.0").format(distanceInKm)} KM dari $campusShortName"
        }
    }

    private fun hideKosDetailPanel() {
        currentShownKos = null
        binding.kosDetailPanel.root.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_CAMPUS_ID = "extra_campus_id"
        const val EXTRA_CAMPUS_NAME = "extra_campus_name"
    }

}