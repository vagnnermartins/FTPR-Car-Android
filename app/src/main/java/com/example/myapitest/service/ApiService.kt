package com.example.myapitest.service

import com.example.myapitest.model.Item
import com.example.myapitest.model.SingleItem
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("car")
    suspend fun getItems(): List<Item>

    @GET("car/{id}")
    suspend fun getItem(@Path("id") id: String): SingleItem

    @DELETE("car/{id}")
    suspend fun deleteItem(@Path("id") id: String)
}