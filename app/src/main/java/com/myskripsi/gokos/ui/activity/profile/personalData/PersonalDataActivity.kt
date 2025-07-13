package com.myskripsi.gokos.ui.activity.profile.personalData

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.UserProfile
import com.myskripsi.gokos.databinding.ActivityPersonalDataBinding
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PersonalDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalDataBinding
    private val viewModel: PersonalDataViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupDropdowns()
        setupDatePicker()
        setupProfessionListeners()
        setupSaveButton()

        viewModel.loadUserProfile()
        observeViewModel()
    }

    private fun setupDropdowns() {
        val genderItems = resources.getStringArray(R.array.gender_array)
        val genderAdapter = ArrayAdapter(this, R.layout.items_dropdown, genderItems)
        binding.actvGender.setAdapter(genderAdapter)

        val professionItems = resources.getStringArray(R.array.profession_array)
        val professionAdapter = ArrayAdapter(this, R.layout.items_dropdown, professionItems)
        binding.actvProfession.setAdapter(professionAdapter)

        val maritalStatusItems = resources.getStringArray(R.array.marital_status_array)
        val maritalStatusAdapter = ArrayAdapter(this, R.layout.items_dropdown, maritalStatusItems)
        binding.actvMaritalStatus.setAdapter(maritalStatusAdapter)
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
            DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        binding.tilDateOfBirth.setEndIconOnClickListener {
            binding.etDateOfBirth.performClick()
        }
    }

    private fun updateDateInView(calendar: Calendar) {
        val myFormat = "dd MMMM yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale("id", "ID"))
        binding.etDateOfBirth.setText(sdf.format(calendar.time))
    }

    private fun setupProfessionListeners() {
        binding.actvProfession.setOnItemClickListener { _, _, position, _ ->
            val selectedProfession = binding.actvProfession.adapter.getItem(position).toString()
            updateProfessionNameHint(selectedProfession)
        }
    }

    private fun updateProfessionNameHint(profession: String?) {
        when (profession) {
            "Mahasiswa" -> binding.tilProfessionName.hint = getString(R.string.campus_name)
            "Pekerja" -> binding.tilProfessionName.hint = getString(R.string.profession_name)
            else -> binding.tilProfessionName.hint = getString(R.string.detail_profession)
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveChanges.setOnClickListener {
            savePersonalData()
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
                    Toast.makeText(this, "Gagal memuat data: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.saveProfileResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> binding.progressIndicator.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(this,
                        getString(R.string.success_update_personal_data), Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(this, "Gagal menyimpan: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun populateProfileData(profile: UserProfile) {
        binding.actvGender.setText(profile.gender ?: "", false)
        binding.etDateOfBirth.setText(profile.dateOfBirth ?: "")
        binding.actvProfession.setText(profile.profession ?: "", false)
        updateProfessionNameHint(profile.profession)
        binding.etProfessionName.setText(profile.professionName ?: "")
        binding.actvMaritalStatus.setText(profile.maritalStatus ?: "", false)
        binding.etEmergencyContact.setText(profile.emergencyContact ?: "")
    }

    private fun savePersonalData() {
        val gender = binding.actvGender.text.toString()
        val dateOfBirth = binding.etDateOfBirth.text.toString()
        val profession = binding.actvProfession.text.toString()
        val professionName = binding.etProfessionName.text.toString().trim()
        val maritalStatus = binding.actvMaritalStatus.text.toString()
        val emergencyContact = binding.etEmergencyContact.text.toString().trim()

        viewModel.savePersonalData(
            gender.ifEmpty { null },
            dateOfBirth.ifEmpty { null },
            profession.ifEmpty { null },
            professionName.ifEmpty { null },
            maritalStatus.ifEmpty { null },
            emergencyContact.ifEmpty { null }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}