package de.danotter.smooothweather.shared.domain

import android.location.Geocoder
import de.danotter.smooothweather.shared.util.getFromLocationNameAsync

actual class GetPlaceTypeaheadUseCase(
    private val geocoder: Geocoder
) {

    actual suspend operator fun invoke(query: String): PlaceTypeaheadResult {
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