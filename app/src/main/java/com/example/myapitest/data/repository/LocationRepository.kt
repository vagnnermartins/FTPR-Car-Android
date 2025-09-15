package com.example.myapitest.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<LatLng>
}

class LocationRepositoryImpl(
    private val context: Context
) : LocationRepository {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<LatLng> {
        return try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                Result.success(LatLng(location.latitude, location.longitude))
            } else {
                Result.failure(Exception("Não foi possível obter a localização."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}