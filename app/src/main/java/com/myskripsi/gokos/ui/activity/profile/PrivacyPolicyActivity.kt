package com.myskripsi.gokos.ui.activity.profile

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.myskripsi.gokos.databinding.ActivityPrivacyPolicyBinding
import com.myskripsi.gokos.ui.activity.profile.aboutApp.AboutAppActivity

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        setupExpandableSections()
        setupClickListeners()
    }

    private fun setupExpandableSections() {
        binding.headerSection1.setOnClickListener {
            toggleSection(binding.contentSection1, binding.arrowSection1)
        }

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

        binding.headerSection7.setOnClickListener {
            toggleSection(binding.contentSection7, binding.arrowSection7)
        }

    }

    private fun setupClickListeners() {
        binding.tvAboutApp.setOnClickListener {
            startActivity(Intent(this, AboutAppActivity::class.java))
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }

        binding.tvTermsCondition.setOnClickListener {
            startActivity(Intent(this, TermsConditionActivity::class.java))
        }

        binding.tvEmailGokos.setOnClickListener {
            openEmailClient()
        }

        binding.tvNumberGokos.setOnClickListener {
            openWhatsApp()
        }
    }

    private fun openEmailClient() {
        val email = "gokosapp@gmail.com"
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, "Pertanyaan tentang Aplikasi GoKos")
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Tidak ada aplikasi email yang terinstall.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWhatsApp() {
        val phoneNumberWithCountryCode = "62895610787820"
        val url = "https://api.whatsapp.com/send?phone=$phoneNumberWithCountryCode"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Tidak ada aplikasi yang bisa membuka link ini.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSection(contentView: TextView, arrowView: ImageView) {
        if (contentView.visibility == View.GONE) {
            contentView.visibility = View.VISIBLE
            arrowView.animate().rotation(180f).setDuration(300).start()
        } else {
            contentView.visibility = View.GONE
            arrowView.animate().rotation(0f).setDuration(300).start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}