package de.danotter.smooothweather.shared.domain

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import de.danotter.smooothweather.shared.db.PlaceQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

class GetSavedPlacesUseCase(
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
