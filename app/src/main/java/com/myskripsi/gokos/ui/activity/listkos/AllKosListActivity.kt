// File: ui/activity/listkos/AllKosListActivity.kt
package com.myskripsi.gokos.ui.activity.listkos

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.Kos
import com.myskripsi.gokos.databinding.ActivityAllKosListBinding
import com.myskripsi.gokos.ui.activity.detailKos.DetailKosActivity
import com.myskripsi.gokos.ui.adapter.KosAdapter

@Suppress("DEPRECATION")
class AllKosListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllKosListBinding
    private lateinit var kosAdapter: KosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllKosListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra(EXTRA_TITLE)
        val descriptionTitle = intent.getStringExtra(EXTRA_DESC_TITLE)
        val kosList = intent.getParcelableArrayListExtra<Kos>(EXTRA_KOS_LIST)

        setupToolbar(title, descriptionTitle)
        setupRecyclerView()

        if (kosList != null) {
            kosAdapter.submitList(kosList.take(15))
        }
    }

    private fun setupToolbar(title: String?, descriptionTitle: String?) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbarTitleMainAppcompat.text = title ?: getString(R.string.list_kos)

        if (!descriptionTitle.isNullOrBlank()) {
            binding.tvDescriptionToolbar.visibility = View.VISIBLE
            binding.tvDescriptionToolbar.text = descriptionTitle
        } else {
            binding.tvDescriptionToolbar.visibility = View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        kosAdapter = KosAdapter()
        binding.rvKost.apply {
            layoutManager = LinearLayoutManager(this@AllKosListActivity)
            adapter = kosAdapter
        }

        kosAdapter.onItemClick = { selectedKos ->
            val intent = Intent(this, DetailKosActivity::class.java).apply {
                putExtra(DetailKosActivity.EXTRA_DETAIL_KOS, selectedKos)
            }
            startActivity(intent)
        }
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DESC_TITLE = "extra_desc_title"
        const val EXTRA_KOS_LIST = "extra_kos_list"
    }
}