package de.danotter.smooothweather.domain

import android.location.Geocoder
import de.danotter.smooothweather.util.getFromLocationNameAsync
import javax.inject.Inject

class GetPlaceTypeaheadUseCase @Inject constructor(
    private val geocoder: Geocoder
) {
    suspend operator fun invoke(query: String): PlaceTypeaheadResult {
        if (query.isBlank()) {
            return PlaceTypeheadSuccessResult(emptyList())
        }

        val addresses = geocoder.getFromLocationNameAsync(
            locationName = query,
            maxResults = 10
        )

        return PlaceTypeheadSuccessResult(
            items = addresses
                .filter { address ->
                     address.locality != null && address.hasLatitude() && address.hasLongitude()
                }
                .mapIndexed { index, address ->
                    PlaceTypeaheadItem(
                        id = index.toString(),
                        name = address.locality,
                        latitude = address.latitude,
                        longitude = address.longitude
                    )
                }
        )
    }
}

sealed interface PlaceTypeaheadResult

class PlaceTypeheadSuccessResult(
    val items: List<PlaceTypeaheadItem>
) : PlaceTypeaheadResult

class PlaceTypeaheadErrorResult(val error: Throwable?): PlaceTypeaheadResult

class PlaceTypeaheadItem(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)
