package com.myskripsi.gokos.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.myskripsi.gokos.data.model.UserProfile
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserProfileRepository(
    private val db: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val context: Context
) {
//    private val usersCollection = firestore.collection("users")
    private val profileImageBucket = "profile-pictures"

    fun getUserProfile(userId: String): Flow<Result<UserProfile?>> = flow {
        emit(Result.Loading)
        try {
            val snapshot = db.collection("users").document(userId)
                .get()
                .await()

            val userProfile = snapshot.toObject(UserProfile::class.java)
            if (userProfile != null) {
                emit(Result.Success(userProfile))
            } else {
                emit(Result.Error("User Profile not found!"))
            }
        } catch (e: Exception) {
            emit(Result.Error("Failed to retrieve user profile data: ${e.message}"))
        }
    }

    fun saveUserProfile(userId: String, userProfile: UserProfile): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val snapshot = db.collection("users").document(userId)
                .set(userProfile)
                .await()

            val authProfileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(userProfile.fullName)

            if (userProfile.profileImageUrl != null) {
                authProfileUpdates.photoUri = Uri.parse(userProfile.profileImageUrl)
            }
            firebaseAuth.currentUser?.updateProfile(authProfileUpdates.build())?.await()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error("Failed to save user profile data: ${e.message}"))
        }
    }

}
