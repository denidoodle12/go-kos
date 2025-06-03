package com.myskripsi.gokos.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.myskripsi.gokos.utils.Result
import com.myskripsi.gokos.utils.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val preferences: SettingPreferences
) {
    fun loginUser(email: String, pass: String): Flow<Result<FirebaseUser>> = flow {
        emit(Result.Loading)
        try {
            val userCredential = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = userCredential?.user
            if (firebaseUser != null) {
                emit(Result.Success(firebaseUser))
            } else {
                emit(Result.Error("Failed to Login: User not found after sign-in process."))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to login."))
        }
    }

    fun loginWithGoogle(idToken: String): Flow<Result<FirebaseUser>> = flow {
        emit(Result.Loading)
        try {
            val userCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(userCredential).await()
            val firebaseUser = authResult?.user
            if (firebaseUser != null) {
                // Jika ini pengguna baru yang login via Google, profilnya otomatis dibuat di Firebase Auth.
                // Anda mungkin ingin menyimpan info tambahan ke Firestore/Database Anda di sini jika perlu,
                // terutama jika Anda ingin menyamakan struktur data pengguna.
                // Untuk saat ini, kita hanya mengembalikan FirebaseUser.
                emit(Result.Success(firebaseUser))
            } else {
                emit(Result.Error("Failed to Login with Google: User not found after sign-in process."))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to login."))
        }
    }
    
    fun registerUser(fullName: String, email: String, pass: String): Flow<Result<FirebaseUser>> = flow {
        emit(Result.Loading)
        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult?.user
            if (firebaseUser != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
                emit(Result.Success(firebaseUser))
            } else {
                emit(Result.Error("Failed signup: User not created successfully."))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "An error occurred during registration."))
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun logoutUser(): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            firebaseAuth.signOut()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to logout."))
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        preferences.setOnboarded(completed)
    }

    fun isOnboardingCompleted(): Flow<Boolean> {
        return preferences.isOnboarded()
    }
}