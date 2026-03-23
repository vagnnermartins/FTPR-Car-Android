package com.utfpr.ftprcar.model

import com.google.gson.annotations.SerializedName

data class Place(
    @SerializedName("lat")  val lat: Double,
    @SerializedName("long") val lng: Double
)

data class Car(
    @SerializedName("id")       val id: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("year")     val year: String,
    @SerializedName("name")     val name: String,
    @SerializedName("licence")  val licence: String,
    @SerializedName("place")    val place: Place
)
