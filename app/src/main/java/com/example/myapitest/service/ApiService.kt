package com.example.myapitest.service


import com.example.myapitest.models.Car
import retrofit2.http.GET

interface ApiService {

    @GET("car")
    suspend fun getCars(): List<Car>

}
