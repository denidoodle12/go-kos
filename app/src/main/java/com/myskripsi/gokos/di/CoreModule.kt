package com.myskripsi.gokos.di

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.myskripsi.gokos.data.KosRepository
import com.myskripsi.gokos.ui.activity.listkos.ListKosViewModel
import com.myskripsi.gokos.ui.fragment.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Module for Firebase services
val firebaseModule = module {
    single {
        Firebase.firestore
    }
}

val repositoryModule = module {
    single {
        KosRepository(get())
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
}

val appModule = listOf(
    firebaseModule,
    repositoryModule,
    viewModelModule,
    utilsModule
)