package com.utfpr.psil.cartrack.data.repositories

import com.google.android.gms.maps.model.LatLng
import com.utfpr.psil.cartrack.data.datasources.PlaceDataSource
import com.utfpr.psil.cartrack.ui.model.PlaceSuggestion
import javax.inject.Inject

class PlacesRepositoryImpl @Inject constructor(private val placeDataSource: PlaceDataSource) :
    PlacesRepository {
    override suspend fun getAutocompleteSuggestions(query: String): List<PlaceSuggestion> {
        if (query.isBlank() || query.length <= 2) return emptyList()
        return placeDataSource.getAutocompletePredictions(query)
    }

    override suspend fun getCoordinatesForPlace(placeId: String): LatLng? {
        return placeDataSource.fetchPlaceDetails(placeId)
    }

    override suspend fun getAddressFromCoordinates(lat: Double, long: Double): String? {
        return placeDataSource.fetchAddressFromCoordinates(lat, long)
    }
}
