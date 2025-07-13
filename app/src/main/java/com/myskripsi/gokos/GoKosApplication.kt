package com.myskripsi.gokos

import android.app.Application
import com.cloudinary.android.MediaManager
import com.myskripsi.gokos.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import com.myskripsi.gokos.BuildConfig

class GoKosApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Cloudinary menggunakan BuildConfig
        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
        )
        MediaManager.init(this, config)

        // Koin initialization
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@GoKosApplication)
            modules(appModule)
        }
    }
}