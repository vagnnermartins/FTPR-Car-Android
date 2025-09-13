package com.example.myapitest.data.service

import com.example.myapitest.domain.model.Car
import com.example.myapitest.domain.model.CarResponse
import com.example.myapitest.domain.model.DeleteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CarApiService {
    @GET("car")
    suspend fun getCars(): Response<List<Car>>

    @GET("car/{id}")
    suspend fun getCarById(@Path("id") id: String): Response<CarResponse>

    @POST("car")
    suspend fun addCar(@Body car: Car): Response<Car>

    @PATCH("car/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body car: Car): Response<Car>

    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") id: String): Response<DeleteResponse>
}