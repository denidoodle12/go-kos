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

        initiateLocationProcess()
        viewModel.fetchCampusList()
        viewModel.loadUserProfile()
    }

    private fun setupSearchAction() {
        binding.searchViewItem.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupClickListeners() {
        binding.btnOtherKosNearby.setOnClickListener {
            val nearbyKosResult = viewModel.nearbyKosState.value
            if (nearbyKosResult is Result.Success && nearbyKosResult.data.isNotEmpty()) {

                val kosListForNewActivity = nearbyKosResult.data.map { kos ->
                    kos.copy(layoutType = KosLayoutType.REGULAR)
                }

                val intent = Intent(requireContext(), AllKosListActivity::class.java).apply {
                    putExtra(AllKosListActivity.EXTRA_TITLE, getString(R.string.kos_terdekat))
                    putExtra(AllKosListActivity.EXTRA_DESC_TITLE,
                        getString(R.string.di_sekitar_anda))
                    putParcelableArrayListExtra(
                        AllKosListActivity.EXTRA_KOS_LIST,
                        ArrayList(kosListForNewActivity)
                    )
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(),
                    getString(R.string.data_kos_terdekat_tidak_tersedia), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBudgetKos.setOnClickListener {
            val budgetKosResult = viewModel.budgetKosState.value
            if (budgetKosResult is Result.Success && budgetKosResult.data.isNotEmpty()) {

                val kosListForNewActivity = budgetKosResult.data.map { kos ->
                    kos.copy(layoutType = KosLayoutType.REGULAR)
                }

                val title = getString(R.string.kos_sesuai_budget)
                val description = when (viewModel.selectedPriceRange.value) {
                    PriceRangeFilter.BELOW_500K -> getString(R.string.list_kos_under_500)
                    PriceRangeFilter.BETWEEN_500K_700K -> getString(R.string.list_kos_500_to_700)
                    PriceRangeFilter.ABOVE_700K -> getString(R.string.list_kos_above_700)
                    else -> getString(R.string.all_kos_according_budget)
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
                Toast.makeText(requireContext(),
                    getString(R.string.data_kos_budget_not_available), Toast.LENGTH_SHORT).show()
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
                    else -> null
                }
                newFilter?.let {
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
            if (location == null &&
                viewModel.nearbyKosState.value !is Result.Success &&
                shimmerNearbyKosLayout.visibility == View.GONE &&
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
            shimmerNearbyKosLayout.stopShimmer()
            shimmerNearbyKosLayout.visibility = View.GONE
            binding.rvNearbyKos.visibility = View.GONE
            binding.tvNearbyKosStatus.visibility = View.GONE

            when (val helperResult = locationHelper.getCurrentLocation()) {
                is LocationResult.Success -> {
                    binding.progressIndicator.visibility = View.GONE
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
            binding.progressIndicator.visibility = View.GONE
            shimmerNearbyKosLayout.stopShimmer()
            shimmerNearbyKosLayout.visibility = View.GONE
            binding.rvNearbyKos.visibility = View.GONE

            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                handlePermissionDeniedUI(showSettingsOption = true)
            } else {
                handlePermissionDeniedUI(showSettingsOption = false)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.location_permission_needed))
            .setMessage(getString(R.string.this_app_requires_location_permission_to_display_kos_nearby_you))
            .setPositiveButton(getString(R.string.grant_permission)) { _, _ ->
                locationHelper.requestLocationPermissions { permissions ->
                    handlePermissionResult(permissions)
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
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
        shimmerNearbyKosLayout.stopShimmer()
        shimmerNearbyKosLayout.visibility = View.GONE
        binding.rvNearbyKos.visibility = View.GONE

        binding.tvNearbyKosStatus.text = getString(R.string.txt_location_permission_denied_nearby_features_cannot_be_used)
        binding.tvNearbyKosStatus.visibility = View.VISIBLE
        binding.progressIndicator.visibility = View.GONE
        Toast.makeText(requireContext(),
            getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()

        if (showSettingsOption) {
            showSettingsDialog()
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.permission_needed))
            .setMessage(getString(R.string.this_app_requires_location_permission_enable))
            .setPositiveButton(getString(R.string.settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
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
        if (locationHelper.hasLocationPermission()) {
            if (viewModel.userLocation.value == null ||
                (viewModel.nearbyKosState.value is Result.Error && shimmerNearbyKosLayout.visibility == View.GONE)) {
                fetchLocationAndNotifyViewModel()
            }
            if (viewModel.campusListState.value !is Result.Success && shimmerCampusLayout.visibility == View.GONE) {
                viewModel.fetchCampusList()
            }
        } else {
            binding.progressIndicator.visibility = View.GONE
            shimmerNearbyKosLayout.stopShimmer()
            shimmerNearbyKosLayout.visibility = View.GONE
            binding.rvNearbyKos.visibility = View.GONE
            binding.tvNearbyKosStatus.text = getString(R.string.txt_allow_location_access_to_see_kos_nearby_you)
            binding.tvNearbyKosStatus.visibility = View.VISIBLE

            if (viewModel.selectedPriceRange.value == null) {
                viewModel.filterKosByBudget(PriceRangeFilter.BELOW_500K)
            } else {
                viewModel.filterKosByBudget(viewModel.selectedPriceRange.value!!)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
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