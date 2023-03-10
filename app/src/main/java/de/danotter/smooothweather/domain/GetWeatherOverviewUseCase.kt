package de.danotter.smooothweather.domain

import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import javax.inject.Inject

/*
 * Fuses data from different use cases into a single observable stream.
 */
class GetWeatherOverviewUseCase @Inject constructor(
    private val getCurrentPlace: GetCurrentPlaceUseCase,
    private val getSavedPlaces: GetSavedPlacesUseCase,
    private val getWeatherData: GetWeatherDataUseCase,
    private val clock: Clock
) {

    suspend operator fun invoke(): Flow<WeatherOverview> {
        val currentTime = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        return getSavedPlaces()
            .map { savedPlaces ->
                listOf(
                    (getCurrentPlace() as? CurrentPlaceSuccessResult)?.toWeatherLocality()
                ) + savedPlaces.map { place ->
                    place.toWeatherLocality()
                }
            }
            .map(List<WeatherLocality?>::filterNotNull)
            .map { localities ->
                localities.asFlow().flatMapMerge { locality ->
                    flowOf(
                        WeatherOverviewItem(
                            placeName = locality.localityName,
                            weatherData = getWeatherData(
                                latitude = locality.latitude,
                                longitude = locality.longitude,
                                currentTime = currentTime
                            )
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

private fun CurrentPlaceSuccessResult.toWeatherLocality(): WeatherLocality {
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
