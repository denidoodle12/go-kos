package com.myskripsi.gokos.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.myskripsi.gokos.data.model.UserProfile
import com.myskripsi.gokos.utils.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserProfileRepository(
    private val db: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val supabaseClient: SupabaseClient,
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

//    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Flow<Result<String>> = flow {
//        emit(Result.Loading)
//        try {
//            val contentResolver = context.contentResolver
//            val inputStream = contentResolver.openInputStream(imageUri)
//            val fileBytes = inputStream?.readBytes() // Ini adalah ByteArray?
//            inputStream?.close()
//
//            if (fileBytes == null) {
//                emit(Result.Error("Gagal membaca file gambar."))
//                Log.e("UserProfileRepo", "fileBytes is null, cannot upload.")
//                return@flow
//            }
//
//            val mimeType = contentResolver.getType(imageUri) ?: "application/octet-stream"
//            val fileExtension = mimeType.substringAfterLast('/') ?: "jpg"
//            val fileName = "profile_${System.currentTimeMillis()}.$fileExtension"
//            val filePath = "$userId/$fileName" // Path di dalam bucket
//
//            Log.d("SupabaseUpload", "Bucket: $profileImageBucket, Path: $filePath, MIME: $mimeType")
//
//            val bucketApi: StorageBucketApi = supabaseClient.storage.from(profileImageBucket)
//
//            // Panggil upload dengan parameter yang lebih eksplisit
//            val uploadedObjectKey: String = bucketApi.upload(
//                path = filePath,
//                body = fileBytes,
//                upsert = true,
//                contentType = mimeType,
//                cacheControl = "max-age=3600" // Nilai default library
//            )
//            Log.d("SupabaseUpload", "Upload successful, object key: $uploadedObjectKey")
//
//            val publicUrl = bucketApi.publicUrl(path = filePath)
//            Log.d("SupabaseUpload", "Public URL: $publicUrl")
//
//            emit(Result.Success(publicUrl))
//
//        } catch (e: Exception) {
//            Log.e("UserProfileRepo", "Supabase upload error: ", e)
//            emit(Result.Error(e.message ?: "Gagal mengupload gambar profil ke Supabase.", e))
//        }
//    }
}
