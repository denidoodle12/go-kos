package com.myskripsi.gokos.ui.activity.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.myskripsi.gokos.R
import com.myskripsi.gokos.ui.MainActivity
import com.myskripsi.gokos.ui.activity.auth.login.LoginActivity
import com.myskripsi.gokos.ui.activity.onboarding.OnboardingActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private val viewModel: SplashScreenViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.decideNextScreen()
        }, SPLASH_DELAY)

        observeNavigation()

    }

    private fun observeNavigation() {
        viewModel.navigationTarget.observe(this) { target ->
            when(target) {
                is SplashNavigationTarget.ToOnboarding -> {
                    startActivity(Intent(this, OnboardingActivity::class.java))
                }
                is SplashNavigationTarget.ToLogin -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                is SplashNavigationTarget.ToMain -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
            finish()
        }
    }

    companion object {
        private const val SPLASH_DELAY = 2000L
    }
}