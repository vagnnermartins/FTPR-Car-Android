package com.example.myapitest.network

import com.example.myapitest.model.Car
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CarApiService {

    @GET("car")
    suspend fun getCars(): List<Car>

    @POST("car")
    suspend fun createCar(@Body car: Car): Car

    @PUT("car/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body car: Car): Car
}
