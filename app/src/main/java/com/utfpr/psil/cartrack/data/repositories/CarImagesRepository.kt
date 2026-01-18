package com.utfpr.psil.cartrack.data.repositories

interface CarImagesRepository {

    suspend fun uploadPhoto(imageBytes: ByteArray, fileName: String): Result<String>
}