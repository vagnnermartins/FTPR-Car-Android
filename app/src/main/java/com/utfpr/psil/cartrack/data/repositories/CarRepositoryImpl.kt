package com.utfpr.psil.cartrack.data.repositories

import com.utfpr.psil.cartrack.data.datasources.CarApiService
import com.utfpr.psil.cartrack.data.model.CarDetailResponse
import com.utfpr.psil.cartrack.data.model.CarRequest
import javax.inject.Inject

class CarRepositoryImpl @Inject constructor(
    private val apiService: CarApiService
) : CarRepository {
    override suspend fun addCar(car: CarRequest) {
        // Chamada POST para a API Node.js
        val response = apiService.addCar(car)
        if (!response.isSuccessful) {
            throw Exception("Erro ao adicionar carro: ${response.code()}")
        }
    }

    override suspend fun fetchAllCars(): List<CarRequest> = apiService.getAllCars()

    override suspend fun removeCar(id: String) {
        val response = apiService.deleteCar(id)
        if (!response.isSuccessful) {
            throw Exception("Erro ao remover carro: ${response.code()}")
        }
    }

    override suspend fun getCarById(id: String): CarDetailResponse = apiService.getCarById(id)

    override suspend fun updateCar(id: String, car: CarRequest) {
        val response = apiService.updateCar(id, car)
        if (!response.isSuccessful) {
            throw Exception("Erro ao atualizar carro: ${response.code()}")
        }
    }
}