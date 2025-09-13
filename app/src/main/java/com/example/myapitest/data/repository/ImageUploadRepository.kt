package com.example.myapitest.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ImageUploadRepository {
    private val storage = FirebaseStorage.getInstance()

    private val storageRef = storage.reference.child("car_images")

    suspend fun uploadCarImage(imageUri: Uri): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child(fileName)

            val uploadTask = imageRef.putFile(imageUri).await()

            val downloadUrl = imageRef.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}