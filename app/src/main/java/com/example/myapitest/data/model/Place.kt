package com.example.myapitest.data.model

import com.google.gson.annotations.SerializedName

// RZ - Modelo para representar a localização geográfica na API.
// Ajustado para usar 'lat' e 'long' conforme o exemplo, mas atento ao erro 'Latitude/Longitude' do servidor.
data class Place(
    @SerializedName("lat") val lat: Double,
    @SerializedName("long") val long: Double
)
