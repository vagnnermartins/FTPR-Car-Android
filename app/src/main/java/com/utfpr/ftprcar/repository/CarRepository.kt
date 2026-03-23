package com.utfpr.ftprcar.repository

import com.utfpr.ftprcar.model.Car
import com.utfpr.ftprcar.network.RetrofitClient

class CarRepository {

    private val api = RetrofitClient.api

    suspend fun getCars(): List<Car> {
        return api.getCars()
    }

    suspend fun addCar(car: Car): Car {
        return api.addCar(car)
    }

    suspend fun updateCar(id: String, car: Car): Car {
        return api.updateCar(id, car)
    }

    suspend fun deleteCar(id: String) {
        api.deleteCar(id)
    }
}
