package de.danotter.smooothweather.domain

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import de.danotter.smooothweather.db.PlaceQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedPlacesUseCase @Inject constructor(
    private val placeQueries: PlaceQueries
) {

    operator fun invoke(): Flow<List<Place>> {
        return placeQueries.getAll { _, name, longitude, latitude ->
            Place(
                placeName = name,
                latitude = latitude,
                longitude = longitude
            )
        }
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
}
