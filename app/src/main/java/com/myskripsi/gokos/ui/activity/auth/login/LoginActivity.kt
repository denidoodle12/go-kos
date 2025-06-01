package com.myskripsi.gokos.ui.activity.auth.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.myskripsi.gokos.databinding.ActivityLoginBinding
import com.myskripsi.gokos.ui.MainActivity
import com.myskripsi.gokos.ui.activity.auth.signup.SignupActivity
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.getCurrentUser() != null) {
            navigateToMainActivity()
            return
        }

        setupAction()
        observeViewModel()
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val pass = binding.password.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.loginUser(email, pass)
            } else {
                Toast.makeText(this, "The column must not be empty!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.goRegister.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { result ->
            when(result) {
                is Result.Loading -> { binding.progressIndicator.visibility = View.VISIBLE }
                is Result.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    AlertDialog.Builder(this).apply {
                        setTitle("Login Success!")
                        setMessage("Login successful, please enter.")
                        setPositiveButton("Login") { _, _ ->
                            val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()
                        }
                        create()
                        show()
                    }
                }
                is Result.Error -> {
                    AlertDialog.Builder(this).apply {
                        setTitle("Failed to Login!")
                        setMessage("An error occurred during the login process.")
                        setPositiveButton("Back") { _, _ ->

                        }
                        create()
                        show()
                    }
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}