// File: ui/activity/detailKos/DetailKosActivity.kt
package com.myskripsi.gokos.ui.activity.detailKos

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MarkerOptions
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.Favorite
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.databinding.ActivityDetailKosBinding
import com.myskripsi.gokos.databinding.ItemsFacilityBinding
import com.myskripsi.gokos.ui.adapter.KosAdapter
import com.myskripsi.gokos.ui.fragment.favorite.FavoriteActionBottomSheet
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.myskripsi.gokos.utils.Result

@Suppress("DEPRECATION")
class DetailKosActivity : AppCompatActivity(), OnMapReadyCallback, FavoriteActionBottomSheet.FavoriteActionListener {

    private lateinit var binding: ActivityDetailKosBinding
    private var googleMap: GoogleMap? = null
    private var currentKos: Kos? = null
    private var currentFavoriteStatus: Favorite? = null
    private var campusNameRef: String? = null

    private val viewModel: DetailKosViewModel by viewModel()
    private lateinit var otherKosAdapter: KosAdapter
    private lateinit var shimmerOtherKosNearby: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailKosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        shimmerOtherKosNearby = binding.shimmerOtherKosNearby
        setupOtherKosRecyclerView()

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        binding.mapViewKosLocation.onCreate(mapViewBundle)

        val dataKos = intent.getParcelableExtra<Kos>(EXTRA_DETAIL_KOS)
        campusNameRef = intent.getStringExtra(EXTRA_CAMPUS_NAME_REF)

        if (dataKos != null) {
            this.currentKos = dataKos
            showData(dataKos)
            binding.mapViewKosLocation.getMapAsync(this)

            viewModel.fetchOtherNearbyKos(dataKos)
            viewModel.checkFavoriteStatus(dataKos.id)
            setupFavoriteButtonListener()
        } else {
            Toast.makeText(this, getString(R.string.failed_load_data_kos), Toast.LENGTH_SHORT).show()
            finish()
        }

        observeViewModel()
    }

    private fun setupFavoriteButtonListener() {
        binding.ivFavorite.setOnClickListener {
            currentKos?.let { kos ->
                val mode = if (currentFavoriteStatus == null) FavoriteActionBottomSheet.Mode.ADD else FavoriteActionBottomSheet.Mode.DELETE
                val bottomSheet = FavoriteActionBottomSheet.newInstance(kos, mode, currentFavoriteStatus?.note)
                bottomSheet.setFavoriteActionListener(this)
                bottomSheet.show(supportFragmentManager, FavoriteActionBottomSheet.TAG)
            }
        }
    }

    override fun onSaveClicked(kos: Kos, note: String) {
        viewModel.addFavorite(kos, note)
    }

    override fun onDeleteClicked(kos: Kos) {
        currentFavoriteStatus?.let {
            viewModel.removeFavorite(it.id, kos.id)
        }
    }

    private fun setupOtherKosRecyclerView() {
        otherKosAdapter = KosAdapter()
        binding.rvOtherKosNearby.apply {
            layoutManager = LinearLayoutManager(this@DetailKosActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = otherKosAdapter
        }

        otherKosAdapter.onItemClick = { selectedKos ->
            val intent = Intent(this, DetailKosActivity::class.java).apply {
                putExtra(EXTRA_DETAIL_KOS, selectedKos)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.otherNearbyKosState.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    shimmerOtherKosNearby.startShimmer()
                    shimmerOtherKosNearby.visibility = View.VISIBLE
                    binding.rvOtherKosNearby.visibility = View.GONE
                }
                is Result.Success -> {
                    shimmerOtherKosNearby.stopShimmer()
                    shimmerOtherKosNearby.visibility = View.GONE
                    if (result.data.isEmpty()) {
                        binding.tvOtherKosNearbyTitle.visibility = View.GONE
                        binding.rvOtherKosNearby.visibility = View.GONE
                    } else {
                        binding.tvOtherKosNearbyTitle.visibility = View.VISIBLE
                        binding.rvOtherKosNearby.visibility = View.VISIBLE
                        otherKosAdapter.submitList(result.data)
                    }
                }
                is Result.Error -> {
                    shimmerOtherKosNearby.stopShimmer()
                    shimmerOtherKosNearby.visibility = View.GONE
                    binding.tvOtherKosNearbyTitle.visibility = View.GONE
                    binding.rvOtherKosNearby.visibility = View.GONE
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.favoriteStatus.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.ivFavorite.isEnabled = false
                }
                is Result.Success -> {
                    binding.ivFavorite.isEnabled = true
                    currentFavoriteStatus = result.data
                    if (result.data != null) {
                        binding.ivFavorite.setImageResource(R.drawable.ic_favorite_filled)
                    } else {
                        binding.ivFavorite.setImageResource(R.drawable.ic_favorite_outline)
                    }
                }
                is Result.Error -> {
                    binding.ivFavorite.isEnabled = false
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.actionResult.observe(this) { result ->
            if (result is Result.Success) {
                Toast.makeText(this, result.data, Toast.LENGTH_SHORT).show()
            } else if (result is Result.Error) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        binding.mapViewKosLocation.onSaveInstanceState(mapViewBundle)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.uiSettings?.setAllGesturesEnabled(false)

        currentKos?.let { kos ->
            if (kos.lokasi.latitude != 0.0 && kos.lokasi.longitude != 0.0) {
                val kosLocation = com.google.android.gms.maps.model.LatLng(
                    kos.lokasi.latitude,
                    kos.lokasi.longitude
                )
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(kosLocation)
                        .title(kos.nama_kost)
                )
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(kosLocation, 15f)
                )
                binding.mapViewKosLocation.visibility = View.VISIBLE
            } else {
                binding.mapViewKosLocation.visibility = View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun formatDistance(distanceInKm: Double): String {
        return if (distanceInKm < 0) {
            "N/A"
        } else if (distanceInKm < 1.0) {
            val distanceInMeters = distanceInKm * 1000
            "${DecimalFormat("#0").format(distanceInMeters)} m"
        } else {
            "${DecimalFormat("#0.0").format(distanceInKm)} km"
        }
    }

    private fun showData(dataKos: Kos) {
        if (dataKos.foto_kost.isNotEmpty()) {
            Glide.with(this)
                .load(dataKos.foto_kost[0])
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(binding.ivKosMainImage)
        } else {
            binding.ivKosMainImage.setImageResource(R.drawable.placeholder_image)
        }

        if (!campusNameRef.isNullOrBlank()) {
            val campusShortName = when {
                campusNameRef!!.contains("Serang Raya", ignoreCase = true) -> "Unsera"
                campusNameRef!!.contains("Bina Bangsa", ignoreCase = true) -> "Uniba"
                else -> campusNameRef
            }
            val formattedDistance = formatDistance(dataKos.lokasi.jarak)
            binding.tvDistance.text = "$formattedDistance dari $campusShortName"
        } else {
            binding.tvDistance.text = formatDistance(dataKos.lokasi.jarak)
        }

        binding.tvKosType.text = dataKos.kategori
        binding.tvKosName.text = dataKos.nama_kost
        binding.tvKosAddress.text = dataKos.alamat
        binding.tvKosDescription.text = dataKos.deskripsi

        if (dataKos.listrik.trim().contains("Termasuk", ignoreCase = true)) {
            binding.tvListrik.visibility = View.VISIBLE
        } else {
            binding.tvListrik.visibility = View.GONE
        }

        binding.tvLatitude.text = getString(R.string.latitude, dataKos.lokasi.latitude.toString())
        binding.tvLongitude.text = getString(R.string.longitude, dataKos.lokasi.longitude.toString())

        if (dataKos.lokasi.latitude == 0.0 && dataKos.lokasi.longitude == 0.0) {
            binding.KosLocation.visibility = View.GONE
        } else {
            binding.KosLocation.visibility = View.VISIBLE
        }

        binding.tvPrice.text = CURRENCY_FORMATTER.format(dataKos.harga.toDouble())

        populateFacilities(
            binding.llFasilitasKamarContainer,
            dataKos.fasilitas_kamar,
            binding.tvFasilitasKamarTitle
        )

        populateFacilities(
            binding.llFasilitasKamarMandiContainer,
            dataKos.fasilitas_kamar_mandi,
            binding.tvFasilitasKamarMandiTitle
        )
    }

    private fun populateFacilities(
        container: LinearLayout,
        facilities: List<String>,
        titleView: TextView
    ) {
        container.removeAllViews()

        if (facilities.isEmpty()) {
            titleView.visibility = View.GONE
            container.visibility = View.GONE
            if (container.id == binding.llFasilitasKamarMandiContainer.id &&
                (binding.llFasilitasKamarContainer.visibility == View.GONE || binding.llFasilitasKamarContainer.childCount == 0)) {
                binding.divider3.visibility = View.GONE
            }
            return
        }

        titleView.visibility = View.VISIBLE
        container.visibility = View.VISIBLE
        if (binding.llFasilitasKamarContainer.childCount > 0 && binding.llFasilitasKamarMandiContainer.childCount > 0){
            binding.divider3.visibility = View.VISIBLE
        } else if (binding.tvFasilitasKamarTitle.visibility == View.VISIBLE && binding.tvFasilitasKamarMandiTitle.visibility == View.VISIBLE){
            binding.divider3.visibility = View.VISIBLE
        }


        val inflater = LayoutInflater.from(this)
        for (facilityName in facilities) {
            val facilityItemBinding = ItemsFacilityBinding.inflate(inflater, container, false)
            facilityItemBinding.tvFacilityName.text = facilityName
            facilityItemBinding.ivFacilityIcon.setImageResource(getIconForFacility(facilityName))
            container.addView(facilityItemBinding.root)
        }
    }

    private fun getIconForFacility(facilityName: String): Int {
        val normalizedName = facilityName.trim().lowercase(Locale.getDefault())
            .replace(" ", "")
            .replace("-", "")
            .replace("/", "")

        return when {
            normalizedName.contains("kasur") -> R.drawable.ic_facility_bed
            normalizedName.contains("jendela") -> R.drawable.ic_facility_window
            normalizedName.contains("bantal") || normalizedName.contains("guling") -> R.drawable.ic_facility_pillow
            normalizedName.contains("dapurdidalam") -> R.drawable.ic_facility_kitchen
            normalizedName.contains("lemaribaju") -> R.drawable.ic_facility_wardrobe
            normalizedName.contains("kulkas") -> R.drawable.ic_facility_kitchen
            normalizedName.contains("kosongan") -> R.drawable.ic_close_panel

            normalizedName.contains("kamarmandididalam") -> R.drawable.ic_facility_bathroom
            normalizedName.contains("kamarmandidiluar") -> R.drawable.ic_facility_bathroom
            normalizedName.contains("wcjongkok") -> R.drawable.ic_facility_wc
            normalizedName.contains("wcduduk") -> R.drawable.ic_facility_wc
            normalizedName.contains("embermandi") -> R.drawable.ic_facility_bucket
            normalizedName.contains("gayung") -> R.drawable.ic_facility_dipper
            else -> R.drawable.ic_close_panel
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapViewKosLocation.onResume()
        currentKos?.let {
            viewModel.checkFavoriteStatus(it.id)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapViewKosLocation.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapViewKosLocation.onStop()
    }

    override fun onPause() {
        super.onPause()
        shimmerOtherKosNearby.stopShimmer()
        binding.mapViewKosLocation.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapViewKosLocation.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapViewKosLocation.onLowMemory()
    }

    companion object {
        const val EXTRA_DETAIL_KOS = "extra_detail_kos"
        const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        const val EXTRA_CAMPUS_NAME_REF = "extra_campus_name_ref"
        val CURRENCY_FORMATTER: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    }
}