package com.example.myapitest.api

import com.example.myapitest.model.Car
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

interface CarApi {
    @GET("car")
    suspend fun getCars(): List<Car>

    @POST("car")
    suspend fun addCar(@Body car: Car)
}