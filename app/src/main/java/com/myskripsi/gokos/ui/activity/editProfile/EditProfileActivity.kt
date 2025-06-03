package com.myskripsi.gokos.ui.activity.editProfile

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.UserProfile
import com.myskripsi.gokos.databinding.ActivityEditProfileBinding
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModel()
    private var currentProfileImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUploadPhoto.isEnabled = false
        binding.btnUploadPhoto.alpha = 0.5f
        binding.btnDeletePhoto.isEnabled = false
        binding.btnDeletePhoto.alpha = 0.5f

        setupAction()
        setupDropdowns()
        setupDatePicker()
        setupProfessionListeners()

        binding.ivProfileImage.setImageResource(R.drawable.placeholder_image)

        viewModel.loadUserProfile()
        observeViewModel()

    }

    private fun setupAction() {
        binding.btnSaveChanges.setOnClickListener {
            saveProfileChanges()
            Toast.makeText(this, "profile picture successfully edited", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupDropdowns() {
        val genderItems = resources.getStringArray(R.array.gender_array)
        val genderAdapter = ArrayAdapter(this, R.layout.items_dropdown, genderItems)
        (binding.tilGender.editText as? AutoCompleteTextView)?.setAdapter(genderAdapter)

        val professionItems = resources.getStringArray(R.array.profession_array)
        val professionAdapter = ArrayAdapter(this, R.layout.items_dropdown, professionItems)
        (binding.tilProfession.editText as? AutoCompleteTextView)?.setAdapter(professionAdapter)

        val maritalStatusItems = resources.getStringArray(R.array.marital_status_array)
        val maritalStatusAdapter = ArrayAdapter(this, R.layout.items_dropdown, maritalStatusItems)
        (binding.tilMaritalStatus.editText as? AutoCompleteTextView)?.setAdapter(maritalStatusAdapter)
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView(calendar)
        }

        binding.etDateOfBirth.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        binding.tilDateOfBirth.setEndIconOnClickListener {
            binding.etDateOfBirth.performClick()
        }
    }

    private fun updateDateInView(calendar: Calendar) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDateOfBirth.setText(sdf.format(calendar.time))
    }

    private fun setupProfessionListeners() {
        (binding.tilProfession.editText as? AutoCompleteTextView)?.setOnItemClickListener { _, _, position, _ ->
            val selectedProfession = (binding.tilProfession.editText as AutoCompleteTextView).adapter.getItem(position).toString()
            updateProfessionNameHint(selectedProfession)
        }
    }

    private fun updateProfessionNameHint(profession: String?) {
        when (profession) {
            "Mahasiswa" -> {
                binding.tilProfessionName.hint = "Nama Kampus"
                binding.etProfessionName.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            }
            "Pekerja" -> {
                binding.tilProfessionName.hint = "Nama Instansi/Perusahaan"
                binding.etProfessionName.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            }
            else -> { // Termasuk "Lainnya" atau jika field kosong/null
                binding.tilProfessionName.hint = "Detail Profesi"
                binding.etProfessionName.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            }
        }
        // Kosongkan field nama profesi setiap kali profesi berubah agar pengguna mengisi ulang
        // binding.etProfessionName.text = null
    }

    private fun observeViewModel() {
        viewModel.userProfileState.observe(this) { result ->
            when(result) {
                is Result.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.btnSaveChanges.isEnabled = false
                }
                is Result.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    binding.btnSaveChanges.isEnabled = true
                    result.data?.let { userProfile ->
                        populateProfileData(userProfile)
                        currentProfileImageUrl = userProfile.profileImageUrl // Simpan URL gambar yang ada

                        // Muat gambar profil yang sudah ada jika URL tersedia (opsional, jika pakai Glide)
                        Glide.with(this)
                            .load(currentProfileImageUrl ?: R.drawable.placeholder_image)
                            .circleCrop()
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .into(binding.ivProfileImage)
                    }
                }
                is Result.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    binding.btnSaveChanges.isEnabled = true
                    Toast.makeText(this, "Gagal memuat profil: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.saveProfileResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.btnSaveChanges.isEnabled = false
                }
                is Result.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    binding.btnSaveChanges.isEnabled = true
                    Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    finish() // Tutup EditProfileActivity dan kembali ke layar sebelumnya
                }
                is Result.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    binding.btnSaveChanges.isEnabled = true
                    Toast.makeText(this, "Gagal menyimpan profil: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun populateProfileData(profile: UserProfile) {
        binding.etFullName.setText(profile.fullName)
        (binding.tilGender.editText as? AutoCompleteTextView)?.setText(profile.gender ?: "", false)
        binding.etDateOfBirth.setText(profile.dateOfBirth ?: "")
        (binding.tilProfession.editText as? AutoCompleteTextView)?.setText(profile.profession ?: "", false)
        // Update hint profesi berdasarkan data yang dimuat dari ViewModel
        updateProfessionNameHint(profile.profession)
        binding.etProfessionName.setText(profile.professionName ?: "")
        (binding.tilMaritalStatus.editText as? AutoCompleteTextView)?.setText(profile.maritalStatus ?: "", false)
        binding.etEmergencyContact.setText(profile.emergencyContact ?: "")
    }

    private fun saveProfileChanges() {
        // Validasi bisa ditambahkan di sini sebelum mengirim ke ViewModel
        val fullName = binding.etFullName.text.toString().trim()
        val gender = (binding.tilGender.editText as? AutoCompleteTextView)?.text.toString()
        val dateOfBirth = binding.etDateOfBirth.text.toString() // Validasi format tanggal jika perlu
        val profession = (binding.tilProfession.editText as? AutoCompleteTextView)?.text.toString()
        val professionName = binding.etProfessionName.text.toString().trim()
        val maritalStatus = (binding.tilMaritalStatus.editText as? AutoCompleteTextView)?.text.toString()
        val emergencyContact = binding.etEmergencyContact.text.toString().trim()

        if (fullName.isBlank()) {
            binding.tilFullName.error = "Nama tidak boleh kosong"
            // Anda bisa fokus ke field ini juga: binding.etFullName.requestFocus()
            return
        } else {
            binding.tilFullName.error = null
        }
        // Tambahkan validasi lain untuk field lain jika perlu

        viewModel.saveUserProfile(
            fullName,
            gender.ifEmpty { null },
            dateOfBirth.ifEmpty { null },
            profession.ifEmpty { null },
            professionName.ifEmpty { null },
            maritalStatus.ifEmpty { null },
            emergencyContact.ifEmpty { null },// Menggunakan URL gambar yang sudah ada/null
        )
    }

}