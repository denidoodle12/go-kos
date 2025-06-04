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
import androidx.appcompat.app.AlertDialog
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
                Log.w(TAG, "Login Google dibatalkan atau gagal, resultCode: ${result.resultCode}")
                Toast.makeText(this, "Login dengan Google dibatalkan.", Toast.LENGTH_SHORT).show()
                setLoadingState(false) // Sembunyikan loading
            }
        }

        // Cek jika user sudah login
        if (viewModel.getCurrentUser() != null) {
            navigateToMainActivity()
            return // Keluar dari onCreate agar tidak menjalankan setup lainnya
        }

        setupAction() // Ganti nama dari setupListeners() jika perlu agar konsisten
        observeViewModel()
    }

    private fun setupAction() { // Sebelumnya bernama setupListeners
        binding.btnLogin.setOnClickListener {
            val email = binding.email.text.toString().trim() // Asumsikan ID EditText email adalah "email"
            val pass = binding.password.text.toString().trim() // Asumsikan ID EditText password adalah "password"

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.loginUser(email, pass) // Pastikan metode ini ada di ViewModel
            } else {
                Toast.makeText(this, "Kolom email dan password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.goRegister.setOnClickListener { // Asumsikan ID TextView/Button adalah "goRegister"
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Listener untuk tombol Google Sign-In
        // Pastikan Anda memiliki tombol dengan ID "btnGoogleLogin" di activity_login.xml
        binding.btnGoogleLogin?.setOnClickListener { // Menggunakan safe call jika tombol opsional
            setLoadingState(true) // Tampilkan loading
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "Google Sign-In sukses. Account Email: ${account.email}")

            if (account.idToken != null) {
                Log.d(TAG, "ID Token diterima.")
                // viewModel sudah diinisialisasi, _loginState.value = Result.Loading sudah dihandle di ViewModel atau di observe
                viewModel.loginWithGoogleToken(account.idToken!!) // Panggil metode di ViewModel
            } else {
                Log.w(TAG, "Google Sign-In berhasil tapi ID Token null.")
                Toast.makeText(this, "Gagal mendapatkan ID Token dari Google.", Toast.LENGTH_SHORT).show()
                setLoadingState(false) // Sembunyikan loading
            }
        } catch (e: ApiException) {
            Log.w(TAG, "Google Sign-In gagal, code: ${e.statusCode}, message: ${e.message}")
            Toast.makeText(this, "Login Google Gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            setLoadingState(false) // Sembunyikan loading
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
                    // AlertDialog yang sudah Anda buat sebelumnya:
                    AlertDialog.Builder(this).apply {
                        setTitle("Login Berhasil!")
                        setMessage("Login berhasil, selamat datang ${result.data.displayName ?: result.data.email}.")
                        setPositiveButton("Lanjut") { _, _ ->
                            navigateToMainActivity()
                        }
                        setCancelable(false) // Agar tidak bisa di-dismiss dengan back button
                        create()
                        show()
                    }
                }
                is Result.Error -> {
                    setLoadingState(false)
                    // AlertDialog yang sudah Anda buat sebelumnya:
                    AlertDialog.Builder(this).apply {
                        setTitle("Gagal Login!")
                        setMessage(result.message ?: "Terjadi kesalahan saat proses login.")
                        setPositiveButton("Kembali") { dialog, _ ->
                            dialog.dismiss()
                        }
                        create()
                        show()
                    }
                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false
            binding.btnGoogleLogin?.isEnabled = false // Nonaktifkan tombol Google juga
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