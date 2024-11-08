package com.example.myapitest.service

import com.example.myapitest.model.Item
import retrofit2.http.GET

interface ApiService {
    @GET("car")
    suspend fun getItems(): List<Item>
}