package com.example.myapitest.service

import com.example.myapitest.model.Car
import com.example.myapitest.model.CarDetailResponse
import retrofit2.Response
import retrofit2.http.*

interface CarApiService {
    @GET("car")
    suspend fun getCars(): List<Car>

    @GET("car/{id}")
    suspend fun getCar(@Path("id") id: String): CarDetailResponse

    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") id: String): Response<Unit>

    @PATCH("car/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body car: Car): Car

    @POST("car")
    suspend fun addCar(@Body car: Car)
}
