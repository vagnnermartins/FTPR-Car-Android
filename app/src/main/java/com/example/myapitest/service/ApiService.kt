package com.example.myapitest.service


import com.example.myapitest.models.Car
import com.example.myapitest.models.ResponseCarWrapper
import retrofit2.http.DELETE

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("car")
    suspend fun fetchCars(): List<Car>

    @GET("car/{id}")
    suspend fun getCar(@Path("id") id: String): ResponseCarWrapper

    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") id: String)
}
