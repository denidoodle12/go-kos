package com.myskripsi.gokos.ui.activity.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.AuthRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(private val repository: AuthRepository) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
        }
    }

}