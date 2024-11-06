package com.example.myapitest.models

data class Car(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
)

data class ResponseCarWrapper(
    val value: Car
)