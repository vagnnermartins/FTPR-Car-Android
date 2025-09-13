package com.example.myapitest.data.models.response

import kotlinx.serialization.Serializable

@Serializable
data class CreateCarResponseResult(
    val id: String,
    val name: String,
    val year: String,
    val license: String,
    val imageUrl: String,
    val lat: String,
    val long: String
)