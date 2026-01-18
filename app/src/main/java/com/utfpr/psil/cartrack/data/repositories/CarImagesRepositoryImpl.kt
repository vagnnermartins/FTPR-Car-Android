package com.utfpr.psil.cartrack.data.repositories

import com.utfpr.psil.cartrack.data.datasources.CarImagesDataSource
import javax.inject.Inject

class CarImagesRepositoryImpl @Inject constructor(
    private val carImagesDataSource: CarImagesDataSource
) :
    CarImagesRepository {
    override suspend fun uploadPhoto(imageBytes: ByteArray, fileName: String): Result<String> {
        return carImagesDataSource.uploadPhoto(imageBytes, fileName)
    }
}