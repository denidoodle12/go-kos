// EditProfileActivity.kt
package com.myskripsi.gokos.ui.activity.profile.editProfile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.UserProfile
import com.myskripsi.gokos.databinding.ActivityEditProfileBinding
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // TODO: Fungsionalitas upload & hapus foto akan ditambahkan nanti
//        binding.btnUploadPhoto.isEnabled = false
//        binding.btnDeletePhoto.isEnabled = false

        setupSaveButton()

        viewModel.loadUserProfile()
        observeViewModel()
    }

    private fun setupSaveButton(){
        // ID tombol di XML Anda adalah btnLogout, sebaiknya diganti menjadi btnSaveChanges atau sejenisnya
        binding.btnSaveChanges.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            if (fullName.isBlank()) {
                binding.tilFullName.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }
            binding.tilFullName.error = null
            viewModel.saveProfileName(fullName)
        }
    }

    private fun observeViewModel() {
        viewModel.userProfileState.observe(this) { result ->
            when(result) {
                is Result.Loading -> binding.progressIndicator.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    result.data?.let { populateProfileData(it) }
                }
                is Result.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(this, "Gagal memuat profil: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.saveProfileResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> binding.progressIndicator.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(this, "Gagal menyimpan profil: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun populateProfileData(profile: UserProfile) {
        binding.etFullName.setText(profile.fullName)
        binding.tvEmailUser.text = profile.email // Tampilkan email

        // Muat gambar profil
        Glide.with(this)
            .load(profile.profileImageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(binding.ivProfileImage)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}