package de.danotter.smooothweather.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import javax.inject.Inject

/*
 * Fuses data from different use cases into a single observable stream.
 */
class GetWeatherOverviewUseCase @Inject constructor(
    private val getCurrentPlace: GetCurrentPlaceUseCase,
    private val getSavedPlaces: GetSavedPlacesUseCase,
    private val getWeatherData: GetWeatherDataUseCase
) {

    suspend operator fun invoke(): Flow<WeatherOverview> {
        return getSavedPlaces()
            .map { savedPlaces ->
                listOf(
                    (getCurrentPlace() as? CurrentPlaceSuccessResult)?.toWeatherLocality()
                ) + savedPlaces.map { place ->
                    place.toWeatherLocality()
                }
            }
            .map(List<WeatherLocality?>::filterNotNull)
            .map {
                it.asFlow().map { locality ->
                    WeatherOverviewItem(
                        placeName = locality.localityName,
                        weatherData = getWeatherData(
                            latitude = locality.latitude,
                            longitude = locality.longitude
                        )
                    )

                }.toList()
            }
            .map(::WeatherOverview)
    }
}

data class WeatherOverview(
    val items: List<WeatherOverviewItem>
)

data class WeatherOverviewItem(
    val placeName: String?,
    val weatherData: WeatherData
)


private data class WeatherLocality(
    val longitude: Double,
    val latitude: Double,
    val localityName: String?
)

private fun CurrentPlaceSuccessResult.toWeatherLocality(): WeatherLocality? {
    if (longitude == null || latitude == null) return null
    return WeatherLocality(
        longitude = longitude,
        latitude = latitude,
        localityName = locality
    )
}

private fun Place.toWeatherLocality(): WeatherLocality {
    return WeatherLocality(
        longitude = longitude,
        latitude = latitude,
        localityName = placeName
    )
}
