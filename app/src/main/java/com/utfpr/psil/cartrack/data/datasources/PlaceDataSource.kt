package com.utfpr.psil.cartrack.data.datasources

import com.google.android.gms.maps.model.LatLng
import com.utfpr.psil.cartrack.ui.model.PlaceSuggestion

interface PlaceDataSource {
    suspend fun getAutocompletePredictions(query: String): List<PlaceSuggestion>
    suspend fun fetchPlaceDetails(placeId: String): LatLng?
    suspend fun fetchAddressFromCoordinates(lat: Double, long: Double): String?
}