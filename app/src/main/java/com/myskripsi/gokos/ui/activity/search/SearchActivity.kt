// File: ui/activity/search/SearchActivity.kt
package com.myskripsi.gokos.ui.activity.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myskripsi.gokos.databinding.ActivitySearchBinding
import com.myskripsi.gokos.ui.activity.detailKos.DetailKosActivity
import com.myskripsi.gokos.ui.activity.listkos.ListKosActivity
import com.myskripsi.gokos.ui.adapter.CampusAdapter
import com.myskripsi.gokos.ui.adapter.KosAdapter
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModel()

    private lateinit var campusAdapter: CampusAdapter
    private lateinit var searchResultsAdapter: KosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerViews()
        setupSearchView()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Judul sudah diatur di XML
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
            // Tidak perlu aksi saat tombol search di keyboard ditekan
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            // Aksi terjadi saat pengguna mengetik
            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty()
                if (query.isBlank()) {
                    // Jika query kosong, tampilkan opsi kampus
                    binding.rvCampusOptions.visibility = View.VISIBLE
                    binding.rvSearchResults.visibility = View.GONE
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
            // Bisa ditambahkan penanganan error/loading jika perlu
        }

        viewModel.searchResultsState.observe(this) { kosList ->
            searchResultsAdapter.submitList(kosList)
        }
    }
}