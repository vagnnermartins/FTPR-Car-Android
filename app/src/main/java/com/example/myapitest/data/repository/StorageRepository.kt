package com.example.myapitest.data.repository

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface StorageRepository {
    suspend fun uploadImage(imageUri: Uri): Result<String>
}

class StorageRepositoryImpl : StorageRepository {
    private val storage = Firebase.storage

    override suspend fun uploadImage(imageUri: Uri): Result<String> {
        return try {
            val fileName = "car_image_${UUID.randomUUID()}"
            val storageRef = storage.reference.child("images/$fileName")

            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}