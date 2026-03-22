package com.example.myapitest.utils

import com.example.myapitest.models.Car
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CarsAPI {

    @GET("car")
    suspend fun getCars(): List<Car>

    @POST("car")
    suspend fun addCar(@Body car: List<Car>): Response<List<Car>>

}
