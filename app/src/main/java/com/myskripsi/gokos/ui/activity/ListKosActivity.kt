package com.myskripsi.gokos.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.myskripsi.gokos.databinding.ActivityListKosBinding
import com.myskripsi.gokos.ui.adapter.KosAdapter
import com.myskripsi.gokos.ui.activity.detailKos.DetailKosActivity // Pastikan import benar

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
            // Judul akan di-set setelah data kampus dimuat
            setDisplayHomeAsUpEnabled(true)
        }

        val campusId = intent.getStringExtra(EXTRA_CAMPUS_ID)

        if (campusId.isNullOrEmpty()) {
            Toast.makeText(this, "ID Kampus tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Panggil fungsi baru di ViewModel
        viewModel.loadKosAndCampusDetails(campusId)

        setupRecyclerView()
        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed() // Cara baru untuk onBackPressed
        return true
    }

    private fun setupRecyclerView() {
        kosAdapter = KosAdapter() // Pastikan KosAdapter Anda sudah ada
        kosAdapter.onItemClick = { selectedData ->
            val intent = Intent(this, DetailKosActivity::class.java).apply {
                putExtra(DetailKosActivity.EXTRA_DETAIL_KOS, selectedData) // selectedData sekarang Parcelable
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
            supportActionBar?.title = name // Set judul ActionBar dengan nama kampus
        }

        viewModel.kosState.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                    showEmpty(false) // Sembunyikan pesan empty saat loading
                    showError(null) // Sembunyikan pesan error saat loading
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
                        kosAdapter.submitList(kosList) // Adapter akan menerima data dengan jarak
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
        if (isEmpty) binding.tvError.text = "Tidak ada kost yang ditemukan di sekitar kampus ini."
    }

    private fun showError(message: String?) {
        binding.tvError.visibility = if (message != null) View.VISIBLE else View.GONE
        binding.tvError.text = message
        if (message != null) binding.rvKost.visibility = View.GONE // Sembunyikan RecyclerView jika ada error
    }

    companion object {
        const val EXTRA_CAMPUS_ID = "extra_campus_id"
        // EXTRA_CAMPUS_NAME tidak lagi diperlukan karena kita mengambilnya dari ViewModel
    }
}