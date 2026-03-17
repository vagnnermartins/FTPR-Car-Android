package com.example.myapitest.repository

import com.example.myapitest.data.api.CarApi
import com.example.myapitest.data.model.Car
import com.example.myapitest.data.model.ItemResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ApiResult(val success: Boolean, val message: String)

class CarRepository(private val carApi: CarApi) {

    // RZ - Busca a lista. Se houver erro de rede, o erro sobe para a Activity tratar a mensagem.
    suspend fun getCars(): List<Car> = withContext(Dispatchers.IO) {
        val response = carApi.fetchCars()
        if (response.isSuccessful) {
            val body = response.body() ?: emptyList()
            body.map { item ->
                val realCar = findDeepData(item.value, 0)
                realCar.copy(id = item.id ?: realCar.id ?: realCar.licence)
            }
        } else {
            throw Exception("Erro API: ${response.code()}")
        }
    }

    private fun findDeepData(car: Car, depth: Int): Car {
        if (depth > 5) return car
        val isPopulated = !car.name.isNullOrBlank() || !car.licence.isNullOrBlank()
        return if (isPopulated) car
        else if (car.nestedValue != null) findDeepData(car.nestedValue, depth + 1)
        else car
    }

    suspend fun saveCarDetailed(car: Car): ApiResult = withContext(Dispatchers.IO) {
        try {
            val idToSend = car.licence ?: ""
            val cleanCar = car.copy(nestedValue = null)
            val response = carApi.saveCar(ItemResponse(id = idToSend, value = cleanCar))
            if (response.isSuccessful) ApiResult(true, "Sucesso!")
            else ApiResult(false, "API ${response.code()}: ${response.errorBody()?.string()}")
        } catch (e: Exception) {
            ApiResult(false, "Falha de conexão")
        }
    }

    suspend fun updateCarDetailed(car: Car): ApiResult = withContext(Dispatchers.IO) {
        val id = car.id ?: car.licence ?: return@withContext ApiResult(false, "ID ausente.")
        try {
            val cleanCar = car.copy(nestedValue = null)
            val response = carApi.updateCar(id, ItemResponse(id = id, value = cleanCar))
            if (response.isSuccessful) ApiResult(true, "Sucesso!")
            else ApiResult(false, "API ${response.code()}")
        } catch (e: Exception) {
            ApiResult(false, "Falha de conexão")
        }
    }

    suspend fun deleteCarDetailed(id: String): ApiResult = withContext(Dispatchers.IO) {
        try {
            val response = carApi.deleteCar(id)
            if (response.isSuccessful) ApiResult(true, "Sucesso!")
            else ApiResult(false, "API ${response.code()}")
        } catch (e: Exception) {
            ApiResult(false, "Falha de conexão")
        }
    }

    suspend fun updateCar(car: Car): Boolean = updateCarDetailed(car).success
    suspend fun deleteCar(id: String): Boolean = deleteCarDetailed(id).success
}
