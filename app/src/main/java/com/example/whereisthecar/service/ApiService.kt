package com.example.whereisthecar.service

import com.example.whereisthecar.model.Car
import com.example.whereisthecar.model.CarGetResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("car")
    suspend fun getCars(): List<Car>

    @GET("car/{id}")
    suspend fun getCarById(@Path("id") id: String): CarGetResponse

    @DELETE("car/{id}")
    suspend fun deleteCarById(@Path("id") id: String)

    @POST("car")
    suspend fun addCar(@Body item: Car): Car

    @PATCH("car/{id}")
    suspend fun updateCarById(@Path("id") id: String, @Body item: Car): Car
}
