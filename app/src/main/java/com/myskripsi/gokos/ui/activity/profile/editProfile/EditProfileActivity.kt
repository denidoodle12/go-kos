package com.myskripsi.gokos.ui.activity.profile.editProfile

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.UserProfile
import com.myskripsi.gokos.databinding.ActivityEditProfileBinding
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModel()

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadAndSaveProfilePicture(uri)
        } else {
            Toast.makeText(this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupClickListeners()

        viewModel.loadUserProfile()
        observeViewModel()
    }

    private fun setupClickListeners(){
        binding.btnSaveChanges.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            if (fullName.isBlank()) {
                binding.tilFullName.error = getString(R.string.name_must_not_be_empty)
                return@setOnClickListener
            }
            binding.tilFullName.error = null
            viewModel.saveProfileName(fullName)
        }

        binding.btnUploadPhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnDeletePhoto.setOnClickListener {
            viewModel.deleteProfilePicture()
        }
    }

    private fun observeViewModel() {
        viewModel.userProfileState.observe(this) { result ->
            when(result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    result.data?.let { populateProfileData(it) }
                }
                is Result.Error -> {
                    showLoading(false)
                    Toast.makeText(this, "Gagal memuat profil: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.saveProfileResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    showLoading(false)
                    Toast.makeText(this, "Gagal menyimpan profil: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun populateProfileData(profile: UserProfile) {
        binding.etFullName.setText(profile.fullName)
        binding.tvEmailUser.text = profile.email

        binding.btnDeletePhoto.isEnabled = !profile.profileImageUrl.isNullOrBlank()

        Glide.with(this)
            .load(profile.profileImageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .fitCenter()
            .centerCrop()
            .into(binding.ivProfileImage)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSaveChanges.isEnabled = !isLoading
        binding.btnUploadPhoto.isEnabled = !isLoading
        if(isLoading) binding.btnDeletePhoto.isEnabled = false
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}