package com.example.myapitest.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ImageRepository {

    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadImage(imageUri: Uri, userId: String): String {
        val fileName = "${System.currentTimeMillis()}_${imageUri.lastPathSegment}"
        val imageRef = storageRef.child("cars/$userId/$fileName")

        return try {
            imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            throw Exception("Falha ao fazer upload da imagem: ${e.message}")
        }
    }
}
