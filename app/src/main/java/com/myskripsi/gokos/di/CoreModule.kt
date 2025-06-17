package com.myskripsi.gokos.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.myskripsi.gokos.data.AuthRepository
import com.myskripsi.gokos.data.FavoriteRepository
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.data.SettingPreferences
import com.myskripsi.gokos.data.UserProfileRepository
import com.myskripsi.gokos.data.dataStore
import com.myskripsi.gokos.ui.activity.auth.login.LoginViewModel
import com.myskripsi.gokos.ui.activity.auth.signup.SignupViewModel
import com.myskripsi.gokos.ui.activity.detailKos.DetailKosViewModel
import com.myskripsi.gokos.ui.activity.profile.editProfile.EditProfileViewModel
import com.myskripsi.gokos.ui.activity.listkos.ListKosViewModel
import com.myskripsi.gokos.ui.activity.map.MappingMapViewModel
import com.myskripsi.gokos.ui.activity.onboarding.OnboardingViewModel
import com.myskripsi.gokos.ui.activity.profile.personalData.PersonalDataViewModel
import com.myskripsi.gokos.ui.activity.splash.SplashScreenViewModel
import com.myskripsi.gokos.ui.fragment.favorite.FavoriteViewModel
import com.myskripsi.gokos.ui.fragment.home.HomeViewModel
import com.myskripsi.gokos.ui.fragment.maps.MapsViewModel
import com.myskripsi.gokos.ui.fragment.profile.ProfileViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://ideyxrussxjefszyoezt.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlkZXl4cnVzc3hqZWZzenlvZXp0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDMxNTI1NDMsImV4cCI6MjA1ODcyODU0M30.AZoH5NAGaJUQT1qPmqXrTO1hxFKR-hLpXxiCulfcPU4"
        ) {
            install(Storage)
        }
    }
    single { get<SupabaseClient>().storage }
}

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
    single {
        UserProfileRepository(get(), get(), get(), androidContext())
    }
    single {
        FavoriteRepository(get())
    }
}

val utilsModule = module {

}

val viewModelModule = module {
    viewModel{
        HomeViewModel(get(), get())
    }
    viewModel {
        ListKosViewModel(get())
    }
    viewModel {
        DetailKosViewModel(get(), get(), get())
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
        ProfileViewModel(get())
    }
    viewModel {
        EditProfileViewModel(get(), get())
    }
    viewModel {
        PersonalDataViewModel(get(), get())
    }
    viewModel {
        SignupViewModel(get())
    }
    viewModel {
        OnboardingViewModel(get())
    }
    viewModel {
        FavoriteViewModel(get(), get(), get())
    }
    viewModel {
        MapsViewModel(get())
    }
}

val appModule = listOf(
    supabaseModule,
    firebaseModule,
    repositoryModule,
    viewModelModule,
    utilsModule,
    dataStoreModule
)