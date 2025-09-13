package com.example.myapitest.data.models.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateCarRequest(
    val id: String,
    val name: String,
    val year: String,
    val licence: String,
    val imageUrl: String,
    val place: Place
)

@Serializable
data class Place(
    val lat: Double,
    val long: Double
)
