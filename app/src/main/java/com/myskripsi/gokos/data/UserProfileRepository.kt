// File: data/UserProfileRepository.kt
package com.myskripsi.gokos.data

import android.content.Context
import android.net.Uri
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.myskripsi.gokos.data.model.UserProfile
import com.myskripsi.gokos.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class UserProfileRepository(
    private val db: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val cloudinary: Cloudinary
) {
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
            db.collection("users").document(userId)
                .set(userProfile)
                .await()

            val authProfileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(userProfile.fullName)
                .setPhotoUri(userProfile.profileImageUrl?.let { Uri.parse(it) })
                .build()
            firebaseAuth.currentUser?.updateProfile(authProfileUpdates)?.await()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error("Failed to save user profile data: ${e.message}"))
        }
    }

    fun uploadProfileImage(userId: String, imageUri: Uri): Flow<Result<Pair<String, String>>> = flow {
        emit(Result.Loading)
        val result = suspendCancellableCoroutine<Result<Pair<String, String>>> { continuation ->
            // Menggunakan userId sebagai public_id agar mudah menimpanya (overwrite)
            val publicId = "gokos_profiles/$userId"

            val requestId = MediaManager.get().upload(imageUri)
                .option("public_id", publicId)
                .option("overwrite", true)
                .option("quality", "auto:good")
                .option("width", 1024)
                .option("height", 1024)
                .option("crop", "limit")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val url = resultData?.get("secure_url") as? String
                        val publicIdResult = resultData?.get("public_id") as? String
                        if (url != null && publicIdResult != null) {
                            continuation.resume(Result.Success(Pair(url, publicIdResult)))
                        } else {
                            continuation.resume(Result.Error("Gagal mendapatkan URL dari Cloudinary."))
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        continuation.resume(Result.Error(error?.description ?: "Gagal mengunggah gambar."))
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()

            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
            }
        }
        emit(result)
    }.flowOn(Dispatchers.IO)

    fun deleteProfileImage(publicId: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            // Menggunakan emptyMap() adalah praktik yang lebih aman
            cloudinary.uploader().destroy(publicId, emptyMap<String, Any>())
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Gagal menghapus gambar dari Cloudinary."))
        }
    }.flowOn(Dispatchers.IO)
}