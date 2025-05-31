package com.myskripsi.gokos.ui.fragment.onboardingscreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myskripsi.gokos.R
import com.myskripsi.gokos.databinding.FragmentFirstScreenBinding
import com.myskripsi.gokos.ui.activity.onboarding.OnboardingActivity

class FirstScreen : Fragment() {

    private var _binding: FragmentFirstScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstScreenBinding.inflate(layoutInflater, container, false)

        binding.btnNext.setOnClickListener {
            (activity as? OnboardingActivity)?.navigateToNextOnboardingPage()
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = FirstScreen()
    }
}