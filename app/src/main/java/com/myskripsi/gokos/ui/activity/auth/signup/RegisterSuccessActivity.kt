package com.myskripsi.gokos.ui.activity.auth.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myskripsi.gokos.databinding.ActivityRegisterSuccessBinding
import com.myskripsi.gokos.ui.MainActivity

class RegisterSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnExplore.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}