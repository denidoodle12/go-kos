package com.myskripsi.gokos.ui.activity.auth.login

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.myskripsi.gokos.R
import com.myskripsi.gokos.databinding.ActivityLoginBinding
import com.myskripsi.gokos.ui.MainActivity
import com.myskripsi.gokos.ui.activity.auth.signup.SignupActivity
import com.myskripsi.gokos.ui.fragment.customalertdialog.CustomAlertDialogFragment
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModel()

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleGoogleSignInResult(task)
            } else {
                Toast.makeText(this,
                    getString(R.string.txt_login_google_cancelled), Toast.LENGTH_SHORT).show()
                setLoadingState(false)
            }
        }

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
                Toast.makeText(this,
                    getString(R.string.txt_email_password_must_filled), Toast.LENGTH_SHORT).show()
            }
        }

        binding.goRegister.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.btnGoogleLogin?.setOnClickListener {
            setLoadingState(true)
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)

            if (account.idToken != null) {
                viewModel.loginWithGoogleToken(account.idToken!!)
            } else {
                Toast.makeText(this,
                    getString(R.string.txt_failed_get_id_token_google), Toast.LENGTH_SHORT).show()
                setLoadingState(false)
            }
        } catch (e: ApiException) {
            Toast.makeText(this,
                getString(R.string.txt_failed_login_google, e.localizedMessage), Toast.LENGTH_LONG).show()
            setLoadingState(false)
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { result ->
            when(result) {
                is Result.Loading -> {
                    setLoadingState(true)
                }
                is Result.Success -> {
                    setLoadingState(false)
                    val successDialog = CustomAlertDialogFragment.newInstance(
                        R.drawable.ic_certificate_success_filled,
                        getString(R.string.txt_success_dialog),
                        getString(
                            R.string.txt_description_info_dialog,
                            result.data.displayName ?: result.data.email
                        ),
                        getString(R.string.txt_next_dialog)
                    )
                    successDialog.setOnDialogActionListener {
                        navigateToMainActivity()
                    }
                    successDialog.show(supportFragmentManager, "SuccessLoginDialog")
                }
                is Result.Error -> {
                    setLoadingState(false)
                    val errorDialog = CustomAlertDialogFragment.newInstance(
                        R.drawable.ic_falied_filled,
                        getString(R.string.txt_failed_dialog),
                        result.message,
                        getString(R.string.txt_try_again_dialog)
                    )
                    errorDialog.show(supportFragmentManager, "ErrorLoginDialog")
                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false
            binding.btnGoogleLogin?.isEnabled = false
            binding.email.isEnabled = false
            binding.password.isEnabled = false
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true
            binding.btnGoogleLogin?.isEnabled = true
            binding.email.isEnabled = true
            binding.password.isEnabled = true
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