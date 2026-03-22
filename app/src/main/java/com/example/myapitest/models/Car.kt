package com.example.myapitest.models

import com.google.gson.annotations.SerializedName

data class Car(
    @SerializedName("id") val id: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("year") val year: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("licence") val licence: String? = null,
    @SerializedName("place") val place: Place? = null
)

data class Place(
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("long") val long: Double? = null
)

data class ApiErrorResponse(
    @SerializedName("errors") val errors: List<ApiError>? = null
)

data class ApiError(
    @SerializedName("car") val car: Car? = null,
    @SerializedName("error") val error: String? = null
)
