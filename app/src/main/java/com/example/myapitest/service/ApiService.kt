package com.example.myapitest.service

import com.example.myapitest.model.Car
import com.example.myapitest.model.ObjCar
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("car")
    suspend fun postCar(@Body car: Car): Car
    @GET("car")
    suspend fun getCars(): List<Car>
    @GET("car/{id}")
    suspend fun getCarById(@Path("id") id: String): ObjCar
    @PATCH("car/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body car: Car): Car
    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") id: String)
}