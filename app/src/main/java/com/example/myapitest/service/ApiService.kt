package com.example.myapitest.service

import com.example.myapitest.model.Item
import com.example.myapitest.model.SingleItem
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("car")
    suspend fun getItems(): List<Item>

    @GET("car/{id}")
    suspend fun getItem(@Path("id") id: String): SingleItem
}