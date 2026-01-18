package com.utfpr.psil.cartrack.data.datasources

interface CarImagesDataSource {

    suspend fun uploadPhoto(imageBytes: ByteArray, fileName: String): Result<String>
}