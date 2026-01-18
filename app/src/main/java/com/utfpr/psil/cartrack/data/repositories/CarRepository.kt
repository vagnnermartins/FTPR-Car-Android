package com.utfpr.psil.cartrack.data.repositories

import com.utfpr.psil.cartrack.data.model.CarDetailResponse
import com.utfpr.psil.cartrack.data.model.CarRequest

interface CarRepository {

    suspend fun addCar(car: CarRequest)
    suspend fun fetchAllCars(): List<CarRequest>
    suspend fun removeCar(id: String)
    suspend fun getCarById(id: String): CarDetailResponse
    suspend fun updateCar(id: String, car: CarRequest)
}