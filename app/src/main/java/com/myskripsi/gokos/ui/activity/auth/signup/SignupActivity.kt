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

    override fun onConfirm() {
        val fullName = binding.fieldFullName.text.toString().trim()
        val email = binding.fieldEmail.text.toString().trim()
        val password = binding.fieldPassword.text.toString().trim()
        val confirmPassword = binding.confirmPassword.text.toString().trim()
        viewModel.signupUser(fullName, email, password, confirmPassword)
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
                binding.fieldFullName.error = getString(R.string.full_name_can_t_be_empty)
                isValid = false
            }

            if (email.isBlank()) {
                binding.fieldEmail.error = getString(R.string.email_can_t_be_empty)
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.fieldEmail.error = getString(R.string.invalid_email_format)
                isValid = false
            }

            if (password.isBlank()) {
                binding.fieldPassword.error = getString(R.string.password_can_t_be_empty)
                isValid = false
            } else if (password.length < 6) {
                binding.fieldPassword.error =
                    getString(R.string.the_password_must_be_at_least_6_characters_long)
                isValid = false
            }

            if (confirmPassword.isBlank()) {
                binding.confirmPassword.error =
                    getString(R.string.password_confirmation_can_t_be_empty)
                isValid = false
            }

            if (isValid && password.isNotBlank() && confirmPassword.isNotBlank() && password != confirmPassword) {
                binding.confirmPassword.error =
                    getString(R.string.password_and_password_confirmation_do_not_match)
                isValid = false
            }

            if (isValid) {
                ConfirmationDialogFragment.newInstance(
                    getString(R.string.txt_confirmation_dialog),
                    getString(R.string.txt_confirmation2_dialog),
                    getString(R.string.txt_yes_dialog),
                    getString(R.string.txt_no_dialog)
                ).show(supportFragmentManager, "ConfirmSignupDialog")
            } else {
                Toast.makeText(this,
                    getString(R.string.please_fill_in_all_fields_correctly), Toast.LENGTH_SHORT).show()
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

                    startActivity(Intent(this, RegisterSuccessActivity::class.java))
                    finish()
                }
                is Result.Error -> {
                    progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true

                    val errorDialog = CustomAlertDialogFragment.newInstance(
                        R.drawable.ic_falied_filled,
                        getString(R.string.txt_failed_dialog),
                        result.message,
                        getString(R.string.txt_try_again_dialog)
                    )
                    errorDialog.show(supportFragmentManager, "ErrorSignupDialog")
                }
            }
        }
    }
}