// File: ui/fragment/maps/MapsFragment.kt
package com.myskripsi.gokos.ui.fragment.maps

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.Campus
import com.myskripsi.gokos.databinding.FragmentMapsBinding
import com.myskripsi.gokos.ui.activity.map.MappingMapActivity
import com.myskripsi.gokos.ui.adapter.MapCampusAdapter
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapsViewModel by viewModel()
    private lateinit var campusAdapter: MapCampusAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        viewModel.fetchCampuses()
    }

    private fun setupRecyclerView() {
        campusAdapter = MapCampusAdapter()
        binding.rvCampusMaps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = campusAdapter
        }

        campusAdapter.onItemClick = { campus ->
            navigateToMappingMap(campus)
        }
    }

    private fun observeViewModel() {
        viewModel.campusListState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.shimmerLayout.startShimmer()
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.rvCampusMaps.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    if (result.data.isEmpty()) {
                        binding.tvError.text = getString(R.string.data_campus_not_available)
                        binding.tvError.visibility = View.VISIBLE
                    } else {
                        binding.rvCampusMaps.visibility = View.VISIBLE
                        campusAdapter.submitList(result.data)
                    }
                }
                is Result.Error -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.tvError.text = result.message
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun navigateToMappingMap(campus: Campus) {
        val intent = Intent(requireContext(), MappingMapActivity::class.java).apply {
            putExtra(MappingMapActivity.EXTRA_CAMPUS_ID, campus.id)
            putExtra(MappingMapActivity.EXTRA_CAMPUS_NAME, campus.nama_kampus)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}