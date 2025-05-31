package com.myskripsi.gokos.ui.fragment.onboardingscreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myskripsi.gokos.databinding.FragmentThirdScreenBinding
import com.myskripsi.gokos.ui.activity.onboarding.OnboardingActivity

class ThirdScreen : Fragment() {

    private var _binding: FragmentThirdScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThirdScreenBinding.inflate(layoutInflater, container, false)

        binding.btnNext.setOnClickListener {
            (activity as? OnboardingActivity)?.userCompletesOnboarding()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = ThirdScreen()
    }

}