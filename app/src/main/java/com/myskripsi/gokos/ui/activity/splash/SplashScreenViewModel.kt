package com.myskripsi.gokos.ui.activity.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myskripsi.gokos.data.AuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class SplashNavigationTarget {
    object ToOnboarding : SplashNavigationTarget()
    object ToLogin : SplashNavigationTarget()
    object ToMain : SplashNavigationTarget()
}

class SplashScreenViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _navigationTarget = MutableLiveData<SplashNavigationTarget>()
    val navigationTarget: LiveData<SplashNavigationTarget> = _navigationTarget

    fun decideNextScreen() {
        viewModelScope.launch {
            val isOnboardingCompleted = repository.isOnboardingCompleted().first()

            if (!isOnboardingCompleted) {
                _navigationTarget.value = SplashNavigationTarget.ToOnboarding
            } else {
                if (repository.getCurrentUser() != null) {
                    _navigationTarget.value = SplashNavigationTarget.ToMain
                } else {
                    _navigationTarget.value = SplashNavigationTarget.ToLogin
                }
            }
        }
    }
}