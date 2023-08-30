package de.danotter.smooothweather.shared.domain

import de.danotter.smooothweather.shared.db.PlaceQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class AddPlaceUseCase(
    private val placeQueries: PlaceQueries
) {

    suspend operator fun invoke(
        place: Place
    ) {
        withContext(Dispatchers.IO) {
            placeQueries.insert(
                name = place.placeName,
                longitude = place.longitude,
                latitude = place.latitude
            )
        }
    }
}
