package com.example.myapitest.data.api

import com.example.myapitest.data.model.Car
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// RZ - Definição da interface Retrofit para a API REST de carros
interface CarService {

    @GET("car")
    suspend fun fetchCars(): Response<List<Car>>

    @POST("car")
    suspend fun saveCar(@Body car: Car): Response<Car>
}
