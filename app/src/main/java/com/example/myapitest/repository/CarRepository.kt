package com.example.myapitest.repository

import com.example.myapitest.model.Car
import com.example.myapitest.network.CarApiService
import com.example.myapitest.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CarRepository {

    private val apiService = RetrofitClient.instance.create(CarApiService::class.java)

    fun getCars(): Flow<List<Car>> = flow {
        val cars = apiService.getCars()
        emit(cars)
    }.flowOn(Dispatchers.IO)

    fun createCar(car: Car): Flow<Car> = flow {
        val result = apiService.createCar(car)
        emit(result)
    }.flowOn(Dispatchers.IO)

    fun updateCar(id: String, car: Car): Flow<Car> = flow {
        val result = apiService.updateCar(id, car)
        emit(result)
    }.flowOn(Dispatchers.IO)
}
