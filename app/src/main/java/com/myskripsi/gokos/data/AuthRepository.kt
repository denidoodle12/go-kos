package com.myskripsi.gokos.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.myskripsi.gokos.utils.Result
import com.myskripsi.gokos.utils.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val preferences: SettingPreferences
) {
    fun loginUser(email: String, pass: String): Flow<Result<FirebaseUser>> = flow {
        emit(Result.Loading)
        try {
            val userCredential = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val user = userCredential?.user
            if (user != null) {
                emit(Result.Success(user))
            } else {
                emit(Result.Error("Failed to Login: User not found after sign-in process!"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to login."))
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