package com.myskripsi.gokos.ui.activity.profile.aboutApp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.myskripsi.gokos.R
import com.myskripsi.gokos.databinding.ActivityAboutAppBinding
import com.myskripsi.gokos.ui.activity.profile.PrivacyPolicyActivity
import com.myskripsi.gokos.ui.activity.profile.TermsConditionActivity

class AboutAppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutAppBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        setupClickListeners()
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

        // Cek apakah ada aplikasi yang bisa menangani intent ini (browser atau WhatsApp)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Tidak ada aplikasi yang bisa membuka link ini.", Toast.LENGTH_SHORT).show()
        }
    }
}