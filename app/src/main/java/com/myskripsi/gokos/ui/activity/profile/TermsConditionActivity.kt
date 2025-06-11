package com.myskripsi.gokos.ui.activity.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.myskripsi.gokos.R
import com.myskripsi.gokos.databinding.ActivityTermsConditionBinding

class TermsConditionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsConditionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsConditionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        setupExpandableSections()
    }

    private fun setupExpandableSections() {
        binding.headerSection1.setOnClickListener {
            toggleSection(binding.contentSection1, binding.arrowSection1)
        }

        // Section 2
        binding.headerSection2.setOnClickListener {
            toggleSection(binding.contentSection2, binding.arrowSection2)
        }

        binding.headerSection3.setOnClickListener {
            toggleSection(binding.contentSection3, binding.arrowSection3)
        }

        binding.headerSection4.setOnClickListener {
            toggleSection(binding.contentSection4, binding.arrowSection4)
        }

        binding.headerSection5.setOnClickListener {
            toggleSection(binding.contentSection5, binding.arrowSection5)
        }

        binding.headerSection6.setOnClickListener {
            toggleSection(binding.contentSection6, binding.arrowSection6)
        }
    }

    private fun toggleSection(contentView: TextView, arrowView: ImageView) {
        if (contentView.visibility == View.GONE) {
            // Jika tertutup, buka
            contentView.visibility = View.VISIBLE
            arrowView.animate().rotation(180f).setDuration(300).start()
        } else {
            // Jika terbuka, tutup
            contentView.visibility = View.GONE
            arrowView.animate().rotation(0f).setDuration(300).start()
        }
    }

    // Fungsi untuk handle klik tombol kembali di toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

}