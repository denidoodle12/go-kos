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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.myskripsi.gokos.R
import com.myskripsi.gokos.databinding.FragmentHomeBinding
import com.myskripsi.gokos.ui.activity.listkos.ListKosActivity
import com.myskripsi.gokos.ui.activity.detailKos.DetailKosActivity
import com.myskripsi.gokos.ui.activity.listkos.AllKosListActivity
import com.myskripsi.gokos.ui.activity.search.SearchActivity
import com.myskripsi.gokos.ui.adapter.CampusAdapter
import com.myskripsi.gokos.ui.adapter.KosAdapter
import com.myskripsi.gokos.ui.adapter.KosLayoutType
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
    private lateinit var budgetKosAdapter: KosAdapter

    private lateinit var shimmerNearbyKosLayout: ShimmerFrameLayout
    private lateinit var shimmerCampusLayout: ShimmerFrameLayout
    private lateinit var shimmerBudgetKosLayout: ShimmerFrameLayout

    private lateinit var locationHelper: LocationHelper

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            locationHelper.handlePermissionResult(permissions)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationHelper = LocationHelper(requireActivity() as AppCompatActivity, requestPermissionLauncher)
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

        shimmerNearbyKosLayout = binding.shimmerNearbyKosLayout
        shimmerCampusLayout = binding.shimmerCampusLayout
        shimmerBudgetKosLayout = binding.shimmerBudgetKosLayout

        setupNearbyKosRecyclerView()
        setupCampusRecyclerView()
        setupBudgetKosRecyclerView()
        setupBudgetToggleGroupListener()
        setupSearchAction()
        setupClickListeners()

        observeViewModel()

        initiateLocationProcess() // Memulai proses cek izin dan ambil lokasi
        viewModel.fetchCampusList() // Memulai fetch daftar kampus, akan memicu shimmer kampus
        viewModel.loadUserProfile()
    }

    private fun setupSearchAction() {
        binding.searchViewItem.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupClickListeners() {
        // Listener untuk tombol "Lihat Lainnya" pada KOS TERDEKAT
        binding.btnOtherKosNearby.setOnClickListener {
            // Ambil data dari LiveData di ViewModel
            val nearbyKosResult = viewModel.nearbyKosState.value
            if (nearbyKosResult is Result.Success && nearbyKosResult.data.isNotEmpty()) {

                // PENTING: Ubah layoutType agar menggunakan tampilan vertikal
                val kosListForNewActivity = nearbyKosResult.data.map { kos ->
                    kos.copy(layoutType = KosLayoutType.REGULAR)
                }

                val intent = Intent(requireContext(), AllKosListActivity::class.java).apply {
                    putExtra(AllKosListActivity.EXTRA_TITLE, "Kos Terdekat")
                    putExtra(AllKosListActivity.EXTRA_DESC_TITLE, "Di sekitar lokasi Anda saat ini")
                    putParcelableArrayListExtra(
                        AllKosListActivity.EXTRA_KOS_LIST,
                        ArrayList(kosListForNewActivity)
                    )
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Data kos terdekat tidak tersedia.", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener untuk tombol "Lihat Lainnya" pada KOS BUDGET
        binding.btnBudgetKos.setOnClickListener {
            val budgetKosResult = viewModel.budgetKosState.value
            if (budgetKosResult is Result.Success && budgetKosResult.data.isNotEmpty()) {

                // PENTING: Ubah juga layoutType di sini
                val kosListForNewActivity = budgetKosResult.data.map { kos ->
                    kos.copy(layoutType = KosLayoutType.REGULAR)
                }

                // Buat judul dan sub-judul dinamis berdasarkan filter yang aktif
                val title = "Kos Sesuai Budget"
                val description = when (viewModel.selectedPriceRange.value) {
                    PriceRangeFilter.BELOW_500K -> "List Kos di Bawah Rp 500 Ribu"
                    PriceRangeFilter.BETWEEN_500K_700K -> "List Kos Rp 500 - 700 Ribu"
                    PriceRangeFilter.ABOVE_700K -> "List Kos di Atas Rp 700 Ribu"
                    else -> "Semua kos sesuai budget"
                }

                val intent = Intent(requireContext(), AllKosListActivity::class.java).apply {
                    putExtra(AllKosListActivity.EXTRA_TITLE, title)
                    putExtra(AllKosListActivity.EXTRA_DESC_TITLE, description)
                    putParcelableArrayListExtra(
                        AllKosListActivity.EXTRA_KOS_LIST,
                        ArrayList(kosListForNewActivity)
                    )
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Data kos budget tidak tersedia.", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun setupBudgetKosRecyclerView() {
        budgetKosAdapter = KosAdapter()
        budgetKosAdapter.onItemClick = { selectedKos ->
            val intent = Intent(requireContext(), DetailKosActivity::class.java).apply {
                putExtra(DetailKosActivity.EXTRA_DETAIL_KOS, selectedKos)
            }
            startActivity(intent)
        }
        // PERBAIKAN DI SINI:
        binding.rvBudgetKos.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = budgetKosAdapter
        }
    }

    private fun setupBudgetToggleGroupListener() {
        binding.toggleBudgetFilterGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val newFilter = when (checkedId) {
                    R.id.btnFilterBelow500k_toggle -> PriceRangeFilter.BELOW_500K
                    R.id.btnFilter500k700k_toggle -> PriceRangeFilter.BETWEEN_500K_700K
                    R.id.btnFilterAbove700k_toggle -> PriceRangeFilter.ABOVE_700K
                    else -> null // Seharusnya tidak terjadi jika selectionRequired=true
                }
                newFilter?.let {
                    // Hanya panggil jika filter berubah dari state ViewModel saat ini,
                    // untuk menghindari pemanggilan ganda jika check di-set secara programatik.
                    if (viewModel.selectedPriceRange.value != it) {
                        viewModel.filterKosByBudget(it)
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.nearbyKosState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    shimmerNearbyKosLayout.startShimmer()
                    shimmerNearbyKosLayout.visibility = View.VISIBLE
                    binding.rvNearbyKos.visibility = View.GONE
                    binding.tvNearbyKosStatus.visibility = View.GONE
                }
                is Result.Success -> {
                    shimmerNearbyKosLayout.stopShimmer()
                    shimmerNearbyKosLayout.visibility = View.GONE
                    if (result.data.isEmpty()) {
                        binding.tvNearbyKosStatus.text = getString(R.string.txt_cannot_find_kos_nearby_you)
                        binding.tvNearbyKosStatus.visibility = View.VISIBLE
                        binding.rvNearbyKos.visibility = View.GONE
                    } else {
                        binding.tvNearbyKosStatus.visibility = View.GONE
                        binding.rvNearbyKos.visibility = View.VISIBLE
                        nearbyKosAdapter.submitList(result.data.take(5))
                    }
                }
                is Result.Error -> {
                    shimmerNearbyKosLayout.stopShimmer()
                    shimmerNearbyKosLayout.visibility = View.GONE
                    binding.rvNearbyKos.visibility = View.GONE
                    binding.tvNearbyKosStatus.text = result.message
                    binding.tvNearbyKosStatus.visibility = View.VISIBLE
                }
            }
        }

        viewModel.campusListState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    shimmerCampusLayout.startShimmer()
                    shimmerCampusLayout.visibility = View.VISIBLE
                    binding.rvCampusSelection.visibility = View.GONE
                    binding.tvCampusListError.visibility = View.GONE
                }
                is Result.Success -> {
                    shimmerCampusLayout.stopShimmer()
                    shimmerCampusLayout.visibility = View.GONE
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
                    shimmerCampusLayout.stopShimmer()
                    shimmerCampusLayout.visibility = View.GONE
                    binding.rvCampusSelection.visibility = View.GONE
                    binding.tvCampusListError.text = result.message
                    binding.tvCampusListError.visibility = View.VISIBLE
                }
            }
        }

        viewModel.budgetKosState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    shimmerBudgetKosLayout.startShimmer()
                    shimmerBudgetKosLayout.visibility = View.VISIBLE
                    binding.rvBudgetKos.visibility = View.GONE
                    binding.tvBudgetKosStatus.visibility = View.GONE
                }
                is Result.Success -> {
                    shimmerBudgetKosLayout.stopShimmer()
                    shimmerBudgetKosLayout.visibility = View.GONE
                    if (result.data.isEmpty()) {
                        binding.tvBudgetKosStatus.text = getString(R.string.txt_no_kos_found_for_budget)
                        binding.tvBudgetKosStatus.visibility = View.VISIBLE
                        binding.rvBudgetKos.visibility = View.GONE
                    } else {
                        binding.tvBudgetKosStatus.visibility = View.GONE
                        binding.rvBudgetKos.visibility = View.VISIBLE
                        budgetKosAdapter.submitList(result.data)
                    }
                }
                is Result.Error -> {
                    shimmerBudgetKosLayout.stopShimmer()
                    shimmerBudgetKosLayout.visibility = View.GONE
                    binding.rvBudgetKos.visibility = View.GONE
                    binding.tvBudgetKosStatus.text = result.message
                    binding.tvBudgetKosStatus.visibility = View.VISIBLE
                }
            }
        }

        viewModel.selectedPriceRange.observe(viewLifecycleOwner) { filter ->
            // Update UI toggle group jika state filter di ViewModel berubah
            // Ini akan memastikan tombol yang benar terpilih secara visual.
            if (filter != null) {
                val newCheckedId = when (filter) {
                    PriceRangeFilter.BELOW_500K -> R.id.btnFilterBelow500k_toggle
                    PriceRangeFilter.BETWEEN_500K_700K -> R.id.btnFilter500k700k_toggle
                    PriceRangeFilter.ABOVE_700K -> R.id.btnFilterAbove700k_toggle
                }
                if (binding.toggleBudgetFilterGroup.checkedButtonId != newCheckedId) {
                    binding.toggleBudgetFilterGroup.check(newCheckedId)
                }
            }
        }

        viewModel.userLocation.observe(viewLifecycleOwner) { location ->
            // Jika lokasi null DAN shimmer nearby kos tidak terlihat (artinya proses loading selesai/gagal)
            // DAN RecyclerView nearby kos juga tidak terlihat (tidak ada data sukses)
            if (location == null &&
                viewModel.nearbyKosState.value !is Result.Success && // Data kos belum sukses
                shimmerNearbyKosLayout.visibility == View.GONE &&    // Shimmer sudah hilang
                binding.rvNearbyKos.visibility == View.GONE) {       // RecyclerView juga hilang

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
            binding.progressIndicator.visibility = View.VISIBLE // Tampilkan global progress
            // Sembunyikan tampilan terkait kos terdekat saat lokasi belum didapat
            shimmerNearbyKosLayout.stopShimmer() // Pastikan shimmer berhenti jika sebelumnya aktif
            shimmerNearbyKosLayout.visibility = View.GONE
            binding.rvNearbyKos.visibility = View.GONE
            binding.tvNearbyKosStatus.visibility = View.GONE

            when (val helperResult = locationHelper.getCurrentLocation()) {
                is LocationResult.Success -> {
                    binding.progressIndicator.visibility = View.GONE // Sembunyikan global progress
                    viewModel.updateUserLocation(helperResult.location) // Ini akan memicu nearbyKosState
                }
                is LocationResult.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    // Shimmer dan RV sudah disembunyikan di atas
                    binding.tvNearbyKosStatus.text = helperResult.message
                    binding.tvNearbyKosStatus.visibility = View.VISIBLE
                    viewModel.updateUserLocation(null)
                }
                is LocationResult.PermissionDenied -> {
                    binding.progressIndicator.visibility = View.GONE
                    // Shimmer dan RV sudah disembunyikan di atas
                    handlePermissionDeniedUI(showSettingsOption = !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                }
                is LocationResult.LocationDisabled -> {
                    binding.progressIndicator.visibility = View.GONE
                    // Shimmer dan RV sudah disembunyikan di atas
                    binding.tvNearbyKosStatus.text = getString(R.string.txt_location_services_gps_are_disabled_please_enable_them)
                    binding.tvNearbyKosStatus.visibility = View.VISIBLE
                    viewModel.updateUserLocation(null)
                }
                is LocationResult.Loading -> {
                    // binding.progressIndicator sudah visible
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
            // Izin tidak diberikan
            binding.progressIndicator.visibility = View.GONE // Sembunyikan global progress
            // Pastikan shimmer dan RV kos terdekat disembunyikan
            shimmerNearbyKosLayout.stopShimmer()
            shimmerNearbyKosLayout.visibility = View.GONE
            binding.rvNearbyKos.visibility = View.GONE

            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                handlePermissionDeniedUI(showSettingsOption = true) // Pengguna memilih "don't ask again"
            } else {
                handlePermissionDeniedUI(showSettingsOption = false) // Pengguna menolak biasa
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
                // Izin dibatalkan dari dialog rationale
                binding.progressIndicator.visibility = View.GONE
                shimmerNearbyKosLayout.stopShimmer()
                shimmerNearbyKosLayout.visibility = View.GONE
                binding.rvNearbyKos.visibility = View.GONE
                handlePermissionDeniedUI(showSettingsOption = false)
            }
            .create()
            .show()
    }

    private fun handlePermissionDeniedUI(showSettingsOption: Boolean) {
        // Pastikan shimmer dan RV kos terdekat disembunyikan karena izin ditolak
        shimmerNearbyKosLayout.stopShimmer()
        shimmerNearbyKosLayout.visibility = View.GONE
        binding.rvNearbyKos.visibility = View.GONE

        binding.tvNearbyKosStatus.text = getString(R.string.txt_location_permission_denied_nearby_features_cannot_be_used)
        binding.tvNearbyKosStatus.visibility = View.VISIBLE
        binding.progressIndicator.visibility = View.GONE
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
                // Pengguna membatalkan dari dialog pengaturan, pastikan UI konsisten
                if (viewModel.nearbyKosState.value !is Result.Success && !locationHelper.hasLocationPermission()) {
                    shimmerNearbyKosLayout.stopShimmer()
                    shimmerNearbyKosLayout.visibility = View.GONE
                    binding.rvNearbyKos.visibility = View.GONE
                    binding.tvNearbyKosStatus.text = getString(R.string.txt_location_permission_denied_nearby_features_cannot_be_used)
                    binding.tvNearbyKosStatus.visibility = View.VISIBLE
                }
            }
            .create()
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Cek izin saat fragment kembali aktif
        if (locationHelper.hasLocationPermission()) {
            // Jika lokasi belum ada ATAU data kos terdekat masih error (bukan loading/sukses)
            if (viewModel.userLocation.value == null ||
                (viewModel.nearbyKosState.value is Result.Error && shimmerNearbyKosLayout.visibility == View.GONE)) {
                fetchLocationAndNotifyViewModel()
            }
            // Jika daftar kampus belum sukses dan shimmer kampus tidak aktif, fetch lagi
            if (viewModel.campusListState.value !is Result.Success && shimmerCampusLayout.visibility == View.GONE) {
                viewModel.fetchCampusList()
            }
        } else {
            // Izin tidak ada, pastikan UI untuk kos terdekat mencerminkan ini
            binding.progressIndicator.visibility = View.GONE
            shimmerNearbyKosLayout.stopShimmer()
            shimmerNearbyKosLayout.visibility = View.GONE
            binding.rvNearbyKos.visibility = View.GONE
            binding.tvNearbyKosStatus.text = getString(R.string.txt_allow_location_access_to_see_kos_nearby_you)
            binding.tvNearbyKosStatus.visibility = View.VISIBLE

            if (viewModel.selectedPriceRange.value == null) { // Jika belum ada filter, set default
                viewModel.filterKosByBudget(PriceRangeFilter.BELOW_500K)
            } else { // Panggil ulang filter yg ada
                viewModel.filterKosByBudget(viewModel.selectedPriceRange.value!!)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hentikan shimmer secara manual untuk menghindari memory leak jika fragment dihancurkan saat shimmer aktif
        if (::shimmerNearbyKosLayout.isInitialized) {
            shimmerNearbyKosLayout.stopShimmer()
        }
        if (::shimmerCampusLayout.isInitialized) {
            shimmerCampusLayout.stopShimmer()
        }
        if (::shimmerBudgetKosLayout.isInitialized) {
            shimmerBudgetKosLayout.stopShimmer()
        }
        _binding = null
    }
}