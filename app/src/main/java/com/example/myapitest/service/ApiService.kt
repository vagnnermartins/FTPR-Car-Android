package com.example.myapitest.service

import com.example.myapitest.model.CarDto
import com.example.myapitest.model.CarResponse
import com.example.myapitest.model.Car
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.Path


interface ApiService {

    @GET("car") suspend fun getCar(): List<Car>

    @GET("car/{id}") suspend fun getCarById(@Path("id") carId: String): CarResponse

    @POST("car") suspend fun createCar(@Body car: CarDto): Response<Unit>

    @PATCH("car/{id}") suspend fun updateCar(@Path("id") id: String, @Body data: Map<String, @JvmSuppressWildcards Any>): Response<Unit>

    @DELETE("car/{id}") suspend fun deleteCar(@Path("id") id: String): Response<Unit>


}