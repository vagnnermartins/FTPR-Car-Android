package com.example.myapitest.domain.model

data class CarDetail(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val latitude: Double,
    val longitude: Double
)