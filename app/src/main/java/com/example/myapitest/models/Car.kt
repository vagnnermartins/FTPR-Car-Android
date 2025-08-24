package com.example.myapitest.models

data class Car(
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

data class ResponseCarWrapper(
    val id : String,
    val value: Car
)