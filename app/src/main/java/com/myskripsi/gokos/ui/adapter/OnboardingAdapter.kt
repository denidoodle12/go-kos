package com.myskripsi.gokos.ui.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.myskripsi.gokos.ui.fragment.onboardingscreen.FirstScreen
import com.myskripsi.gokos.ui.fragment.onboardingscreen.SecondScreen
import com.myskripsi.gokos.ui.fragment.onboardingscreen.ThirdScreen

class OnboardingAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> FirstScreen.newInstance()
            1 -> SecondScreen.newInstance()
            2 -> ThirdScreen.newInstance()
            else -> throw IllegalStateException("Posisi fragment tidak valid untuk onboarding: $position")
        }
    }
}