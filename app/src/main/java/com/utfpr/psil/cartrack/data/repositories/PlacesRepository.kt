package com.utfpr.psil.cartrack.data.repositories

import com.google.android.gms.maps.model.LatLng
import com.utfpr.psil.cartrack.ui.model.PlaceSuggestion

interface PlacesRepository {
    suspend fun getAutocompleteSuggestions(query: String): List<PlaceSuggestion>
    suspend fun getCoordinatesForPlace(placeId: String): LatLng?
    suspend fun getAddressFromCoordinates(lat: Double, long: Double): String?
}