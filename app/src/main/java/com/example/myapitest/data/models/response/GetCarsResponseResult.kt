package com.example.myapitest.data.models.response

import kotlinx.serialization.Serializable

@Serializable
data class GetCarsResponseResult(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Place
)

data class Place(
    val lat: Double,
    val long: Double
)