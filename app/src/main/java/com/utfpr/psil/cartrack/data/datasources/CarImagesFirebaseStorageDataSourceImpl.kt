package com.utfpr.psil.cartrack.data.datasources

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class CarImagesFirebaseStorageDataSourceImpl : CarImagesDataSource {

    private val storage = FirebaseStorage.getInstance()

    override suspend fun uploadPhoto(imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val storageRef = storage.reference.child("$FIREBASE_PATH/$fileName")
            storageRef.putBytes(imageBytes).await()

            val url = storageRef.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val FIREBASE_PATH = "images"
    }
}
