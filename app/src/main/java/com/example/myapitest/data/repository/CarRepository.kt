package com.example.myapitest.data.repository

import com.example.myapitest.model.Car
import com.example.myapitest.model.CarDto
import com.example.myapitest.model.PlaceDto
import com.example.myapitest.service.ApiService
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.domain.model.CarDetail

interface CarRepository {
    suspend fun getCars(): Result<List<Car>>
    suspend fun getCarById(id: String): Result<CarDetail>
    suspend fun addCar(carDetail: CarDetail): Result<Unit>
    suspend fun updateCar(carDetail: CarDetail): Result<Unit>
    suspend fun deleteCar(id: String): Result<Unit>
}

class CarRepositoryImpl : CarRepository {
    private val apiService: ApiService = RetrofitClient.instance

    override suspend fun getCars(): Result<List<Car>> {
        return try {
            val carDtos = apiService.getCars()
            val cars = carDtos.map { dto ->
                Car(
                    id = dto.id,
                    imageUrl = dto.imageUrl,
                    name = dto.name,
                    licence = "Placa: ${dto.licence}"
                )
            }
            Result.success(cars)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getCarById(id: String): Result<CarDetail> {
        return try {
            val dto = apiService.getCarById(id)
            val carDetail = CarDetail(
                id = dto.id,
                imageUrl = dto.value.imageUrl,
                year = dto.value.year,
                name = dto.value.name,
                licence = dto.value.licence,
                latitude = dto.value.place.lat,
                longitude = dto.value.place.long
            )
            Result.success(carDetail)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun addCar(carDetail: CarDetail): Result<Unit> {
        return try {
            val carDto = CarDto(
                id = carDetail.id,
                imageUrl = carDetail.imageUrl,
                year = carDetail.year,
                name = carDetail.name,
                licence = carDetail.licence,
                place = PlaceDto(lat = carDetail.latitude, long = carDetail.longitude)
            )
            val response = apiService.createCar(carDto)

            if (response.isSuccessful)
                Result.success(Unit)
            else
                Result.failure(Exception("Falha ao adicionar carro"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCar(carDetail: CarDetail): Result<Unit> {
        return try {
            val carDataMap = mapOf(
                "id" to carDetail.id,
                "imageUrl" to carDetail.imageUrl,
                "year" to carDetail.year,
                "name" to carDetail.name,
                "licence" to carDetail.licence,
                "place" to PlaceDto(lat = carDetail.latitude, long = carDetail.longitude)
            )
            val response = apiService.updateCar(carDetail.id, carDataMap)

            if (response.isSuccessful)
                Result.success(Unit)
            else
                Result.failure(Exception("Falha ao atualizar carro"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCar(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteCar(id)
            if (response.isSuccessful)
                Result.success(Unit)
            else
                Result.failure(Exception("Falha ao deletar carro. Código: ${response.code()}"))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}