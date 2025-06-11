package com.myskripsi.gokos.ui.activity.auth.signup

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.myskripsi.gokos.R
import com.myskripsi.gokos.databinding.ActivitySignupBinding
import com.myskripsi.gokos.ui.activity.auth.login.LoginActivity
import com.myskripsi.gokos.ui.fragment.customalertdialog.CustomAlertDialogFragment
import com.myskripsi.gokos.ui.fragment.customalertdialog.ConfirmationDialogFragment
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignupActivity : AppCompatActivity(), ConfirmationDialogFragment.ConfirmationDialogListener {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: SignupViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
        observeViewModel()
    }

    // Fungsi ini adalah callback dari dialog, dipanggil saat user menekan "IYA"
    override fun onConfirm() {
        // Pindahkan logika pemanggilan ViewModel ke sini
        val fullName = binding.fieldFullName.text.toString().trim()
        val email = binding.fieldEmail.text.toString().trim()
        val password = binding.fieldPassword.text.toString().trim()
        val confirmPassword = binding.confirmPassword.text.toString().trim() // Ambil lagi untuk validasi ViewModel
        viewModel.signupUser(fullName, email, password, confirmPassword)
    }

    private fun setupAction() {
        binding.btnRegister.setOnClickListener {
            // Validasi input di sisi UI terlebih dahulu
            val fullName = binding.fieldFullName.text.toString().trim()
            val email = binding.fieldEmail.text.toString().trim()
            val password = binding.fieldPassword.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            // Reset error
            binding.fieldFullName.error = null
            binding.fieldEmail.error = null
            binding.fieldPassword.error = null
            binding.confirmPassword.error = null

            var isValid = true
            // ... (logika validasi Anda tetap di sini) ...
            if (fullName.isBlank()) {
                binding.fieldFullName.error = "Full name can't be empty"
                isValid = false
            }

            if (email.isBlank()) {
                binding.fieldEmail.error = "Email can't be empty"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.fieldEmail.error = "Invalid email format"
                isValid = false
            }

            if (password.isBlank()) {
                binding.fieldPassword.error = "Password can't be empty"
                isValid = false
            } else if (password.length < 6) {
                binding.fieldPassword.error = "The password must be at least 6 characters long."
                isValid = false
            }

            if (confirmPassword.isBlank()) {
                binding.confirmPassword.error = "Password confirmation can't be empty"
                isValid = false
            }

            if (isValid && password.isNotBlank() && confirmPassword.isNotBlank() && password != confirmPassword) {
                binding.confirmPassword.error = "Password and password confirmation do not match"
                isValid = false
            }

            if (isValid) {
                // JANGAN langsung panggil ViewModel, tapi TAMPILKAN DIALOG KONFIRMASI
                ConfirmationDialogFragment.newInstance(
                    "Konfirmasi Pendaftaran",
                    "Apakah Anda yakin ingin mendaftarkan akun anda?",
                    "IYA",
                    "TIDAK"
                ).show(supportFragmentManager, "ConfirmSignupDialog")
            } else {
                Toast.makeText(this, "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.goLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.signupState.observe(this) { result ->
            val progressBar = binding.progressBar

            when (result) {
                is Result.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled = false
                }
                is Result.Success -> {
                    progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true

                    // JANGAN tampilkan dialog lagi, tapi PINDAH KE ACTIVITY SUKSES
                    startActivity(Intent(this, RegisterSuccessActivity::class.java))
                    finish() // Tutup SignupActivity agar tidak bisa kembali
                }
                is Result.Error -> {
                    progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true

                    // Gunakan CustomAlertDialogFragment yang lama untuk menampilkan error
                    val errorDialog = CustomAlertDialogFragment.newInstance(
                        R.drawable.ic_falied_filled,
                        "Registrasi Gagal!",
                        result.message,
                        "Tutup"
                    )
                    errorDialog.show(supportFragmentManager, "ErrorSignupDialog")
                }
            }
        }
    }
}