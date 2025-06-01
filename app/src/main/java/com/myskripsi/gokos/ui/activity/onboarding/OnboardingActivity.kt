package com.myskripsi.gokos.ui.activity.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myskripsi.gokos.databinding.ActivityOnboardingBinding
import com.myskripsi.gokos.ui.adapter.OnboardingAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val viewModel: OnboardingViewModel by viewModel()
    private lateinit var onboardingAdapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupDotsIndicator()
    }

    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter(this)
        binding.viewPager.adapter = onboardingAdapter
    }

    private fun setupDotsIndicator() {
        binding.dotsIndicator.attachTo(binding.viewPager)
    }

    fun userCompletesOnboarding() {
        viewModel.completeOnboarding()
        val intent = Intent(this, StartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    fun navigateToNextOnboardingPage() {
        val currentItem = binding.viewPager.currentItem
        if (currentItem < onboardingAdapter.itemCount - 1) {
            binding.viewPager.currentItem = currentItem + 1
        }
    }
}