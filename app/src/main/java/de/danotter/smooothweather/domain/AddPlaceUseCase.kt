package de.danotter.smooothweather.domain

import de.danotter.smooothweather.db.PlaceQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddPlaceUseCase @Inject constructor(
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
