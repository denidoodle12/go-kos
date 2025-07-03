import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.myskripsi.gokos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.myskripsi.gokos"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- KODE BARU YANG BENAR ---
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", localProperties.getProperty("CLOUDINARY_CLOUD_NAME", ""))
        buildConfigField("String", "CLOUDINARY_API_KEY", localProperties.getProperty("CLOUDINARY_API_KEY", ""))
        buildConfigField("String", "CLOUDINARY_API_SECRET", localProperties.getProperty("CLOUDINARY_API_SECRET", ""))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    // Core Android dependencies
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    // Navigation Component
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // ViewModel and LiveData
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Firebase
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Kotlin Coroutines Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Koin for Dependency Injection
    implementation("io.insert-koin:koin-android:3.5.6")

    // Image Processing
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Google Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Onboarding
    implementation("com.tbuonomo:dotsindicator:5.1.0")

    // Auth
    implementation(libs.firebase.auth.ktx)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // Auth with Google
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Data Store
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Supabase Storage
    implementation("io.ktor:ktor-client-android:2.3.9")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.3.0")

    // Lottie Animation
    implementation("com.airbnb.android:lottie:6.6.4")

    // Shimmer Loading
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Cloudinary
    implementation("com.cloudinary:cloudinary-android:2.4.0")

}