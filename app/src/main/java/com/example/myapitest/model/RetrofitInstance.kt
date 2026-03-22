package com.example.myapitest.network

import com.example.myapitest.model.Car
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface CarApi {
    @GET("/car")
    suspend fun getCars(): List<Car>

    @POST("/car")
    suspend fun createCar(@Body car: Car): Car
}

object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:3000/" // emulador Android

    val api: CarApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CarApi::class.java)
    }
}