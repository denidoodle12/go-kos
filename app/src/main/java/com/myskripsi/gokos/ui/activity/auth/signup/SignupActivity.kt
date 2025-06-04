package com.myskripsi.gokos.ui.activity.auth.signup

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.myskripsi.gokos.databinding.ActivitySignupBinding
import com.myskripsi.gokos.ui.activity.auth.login.LoginActivity
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: SignupViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
        observeViewModel()
    }

    private fun setupAction() {
        binding.btnRegister.setOnClickListener {
            val fullName = binding.fieldFullName.text.toString().trim()
            val email = binding.fieldEmail.text.toString().trim()
            val password = binding.fieldPassword.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            binding.fieldFullName.error = null
            binding.fieldEmail.error = null
            binding.fieldPassword.error = null
            binding.confirmPassword.error = null

            var isValid = true

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
                viewModel.signupUser(fullName, email, password, confirmPassword)
            } else {
                if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()){
                     Toast.makeText(this, "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show()
                }
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
                    Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show()

                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("email_from_signup", result.data.email)
                    }
                    startActivity(intent)
                    finishAffinity()
                }
                is Result.Error -> {
                    progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this, "Registration failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}