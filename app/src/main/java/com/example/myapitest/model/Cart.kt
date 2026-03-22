package com.example.myapitest.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class Car(
    val id: String,
    val name: String,
    val year: String,
    val licence: String,
    val imageUrl: String,
    val place: Place
)

@Parcelize
data class Place(
    val lat: Double,
    val long: Double
) : Parcelable