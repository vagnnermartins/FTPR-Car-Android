package com.utfpr.psil.cartrack.data.model

data class CarRequest(
    val id: String? = null,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: CarLocation
)
