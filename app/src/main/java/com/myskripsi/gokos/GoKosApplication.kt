package com.myskripsi.gokos

import android.app.Application
import com.myskripsi.gokos.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class GoKosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@GoKosApplication)
            modules(appModule)
        }
    }
}