package com.myskripsi.gokos.ui.activity.listkos

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.myskripsi.gokos.R
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.myskripsi.gokos.databinding.ActivityListKosBinding
import com.myskripsi.gokos.ui.adapter.KosAdapter
import com.myskripsi.gokos.ui.activity.detailKos.DetailKosActivity

class ListKosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListKosBinding
    private val viewModel: ListKosViewModel by viewModel()
    private lateinit var kosAdapter: KosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListKosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        setupListKosRecyclerView()
        observeViewModel()

        val campusId = intent.getStringExtra(EXTRA_CAMPUS_ID)

        if (campusId.isNullOrEmpty()) {
            Toast.makeText(this, "Campus ID not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        viewModel.loadKosAndCampusDetails(campusId)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupListKosRecyclerView() {
        kosAdapter = KosAdapter()
        kosAdapter.onItemClick = { selectedData ->
            val intent = Intent(this, DetailKosActivity::class.java).apply {
                putExtra(DetailKosActivity.EXTRA_DETAIL_KOS, selectedData)
                putExtra(DetailKosActivity.EXTRA_CAMPUS_NAME_REF, viewModel.campusName.value)
            }
            startActivity(intent)
        }

        with(binding.rvKost) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = kosAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.campusName.observe(this) { name ->
            binding.campusName.text = name
            kosAdapter.setCampusName(name)
        }

        viewModel.kosState.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                    showEmpty(false)
                    showError(null)
                }
                is Result.Success -> {
                    showLoading(false)
                    val kosList = result.data
                    if (kosList.isEmpty()) {
                        showEmpty(true)
                        showError(null)
                    } else {
                        showEmpty(false)
                        showError(null)
                        kosAdapter.submitList(kosList)
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    showEmpty(false)
                    showError(result.message)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvKost.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showEmpty(isEmpty: Boolean) {
        binding.tvError.visibility = if (isEmpty) View.VISIBLE else View.GONE
        if (isEmpty) binding.tvError.text = getString(R.string.txt_there_are_no_kos_found_around_this_campus)
    }

    private fun showError(message: String?) {
        binding.tvError.visibility = if (message != null) View.VISIBLE else View.GONE
        binding.tvError.text = message
        if (message != null) binding.rvKost.visibility = View.GONE
    }

    companion object {
        const val EXTRA_CAMPUS_ID = "extra_campus_id"
        const val EXTRA_CAMPUS_NAME = "extra_campus_name"
    }
}