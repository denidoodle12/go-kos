package com.myskripsi.gokos.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.myskripsi.gokos.data.AuthRepository
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.SettingPreferences
import com.myskripsi.gokos.data.dataStore
import com.myskripsi.gokos.ui.activity.auth.login.LoginViewModel
import com.myskripsi.gokos.ui.activity.listkos.ListKosViewModel
import com.myskripsi.gokos.ui.activity.map.MappingMapViewModel
import com.myskripsi.gokos.ui.activity.onboarding.OnboardingViewModel
import com.myskripsi.gokos.ui.activity.splash.SplashScreenViewModel
import com.myskripsi.gokos.ui.fragment.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val firebaseModule = module {
    single {
        Firebase.firestore
    }
    single {
        FirebaseAuth.getInstance()
    }
}

val dataStoreModule = module {
    single {
        androidContext().dataStore
    }
    single {
        SettingPreferences(get())
    }
}

val repositoryModule = module {
    single {
        KosRepository(get())
    }
    single {
        AuthRepository(get(), get())
    }
}

val utilsModule = module {

}

val viewModelModule = module {
    viewModel{
        HomeViewModel(get())
    }
    viewModel {
        ListKosViewModel(get())
    }
    viewModel {
        MappingMapViewModel(get())
    }
    viewModel {
        SplashScreenViewModel(get())
    }
    viewModel {
        LoginViewModel(get())
    }
    viewModel {
        OnboardingViewModel(get())
    }
}

val appModule = listOf(
    firebaseModule,
    repositoryModule,
    viewModelModule,
    utilsModule,
    dataStoreModule
)