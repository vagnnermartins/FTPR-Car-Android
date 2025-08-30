package com.example.myapitest.model

data class ObjCar(
    val id: String,
    val value: Car
)

data class Car (
    val id: String,
    var imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Place
)

data class Place (
    val lat: Double,
    val long: Double
)