package com.myskripsi.gokos.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.myskripsi.gokos.data.Result
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.myskripsi.gokos.databinding.ActivityListKosBinding
import com.myskripsi.gokos.ui.adapter.KosAdapter

@Suppress("DEPRECATION")
class ListKosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListKosBinding
    private val viewModel: ListKosViewModel by viewModel()
    private lateinit var kosAdapter: KosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListKosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ActionBar
        supportActionBar?.apply {
            title = "campusName"
            setDisplayHomeAsUpEnabled(true)
        }

        // Get ID Campus from intent
        val campusId = intent.getStringExtra(EXTRA_CAMPUS_ID) ?: ""

        // Fetch Data Kos Based ID Campus
        if (campusId.isNotEmpty()) {
            viewModel.getKosByCampusId(campusId)
        } else {
            Toast.makeText(this, "ID Campus not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        showData()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun showData() {
        kosAdapter = KosAdapter()
        kosAdapter.onItemClick = { selectedData ->
            Toast.makeText(this, "Kos ${selectedData.nama_kost} Chosen", Toast.LENGTH_SHORT).show()
        }

        viewModel.kosState.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    val kosList = result.data
                    if (kosList.isEmpty()) {
                        showEmpty(true)
                    } else {
                        showEmpty(false)
                        kosAdapter.submitList(kosList)
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    showError(result.message)
                }
            }
        }

        with(binding.rvKost) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = kosAdapter
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvKost.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.tvError.visibility = View.GONE
    }

    private fun showEmpty(isEmpty: Boolean) {
        binding.tvError.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.tvError.text = "Tidak ada kost yang ditemukan"
    }

    private fun showError(message: String) {
        binding.tvError.visibility = View.VISIBLE
        binding.rvKost.visibility = View.GONE
        binding.tvError.text = message
    }

    companion object {
        const val EXTRA_CAMPUS_ID = "extra_campus_id"
        const val EXTRA_CAMPUS_NAME = "extra_campus_name"
    }
}