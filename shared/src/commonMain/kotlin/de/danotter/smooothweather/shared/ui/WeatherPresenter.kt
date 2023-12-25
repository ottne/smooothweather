package de.danotter.smooothweather.shared.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import de.danotter.smooothweather.shared.domain.GetWeatherOverviewUseCase
import de.danotter.smooothweather.shared.domain.WeatherType
import de.danotter.smooothweather.shared.util.logError
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime

fun weatherPresenter(
    getWeatherOverview: GetWeatherOverviewUseCase,
    resolveWeatherIcon: (weatherType: WeatherType) -> IconSpec,
    clock: Clock
) = flow {
    emit(WeatherLoadingUiModel)

    emitAll(
        getWeatherOverview()
            .filter { it.items.isNotEmpty() }
            .map { weatherOverview ->
                WeatherSuccessUiModel(
                    clock.now(),
                    weatherPager = WeatherPagerUiModel(
                        pages = weatherOverview.items.map { weatherItem ->
                            val weatherData = weatherItem.weatherData

                            val temperature = weatherData.temperature?.toInt()
                            val icon = weatherData.weatherType?.let(resolveWeatherIcon)

                            val fraction = if (temperature != null) {
                                (temperature + 20).coerceIn(0..40) * (100f / 40f) / 100f
                            } else 0.5f
                            val color = lerp(Color(0xFF1184e8), Color(0xFFB7121F), fraction)
                            WeatherPageUiModel(
                                placeName = weatherItem.placeName.orEmpty(),
                                temperature = temperature,
                                weatherDescription = weatherData.weatherDescription,
                                weatherIcon = icon,
                                feltTemperature = weatherData.apparentTemperature?.toInt(),
                                chanceOfPrecipitation = weatherData.precipitation?.toString() ?: "? %",
                                windSpeed = weatherData.windSpeed?.toInt(),
                                humidityPercentage = weatherData.humidity?.toString() ?: "?",
                                backgroundColor = color,
                                hourlyWeather = weatherItem.weatherData.hourlyWeather.mapIndexed { index, value ->
                                    HourWeatherUiModel(
                                        LocalTime(index, 0),
                                        icon = value.weatherType?.let(resolveWeatherIcon),
                                        temperature = value.temperature?.toInt()
                                    )
                                }
                            )
                        }
                    )
                )
            }
    )
}
    .catch { error ->
        logError("Error loading weather data", error)

        emit(WeatherErrorUiModel)
    }

sealed class WeatherUiModel

object WeatherErrorUiModel : WeatherUiModel()

object WeatherLoadingUiModel : WeatherUiModel()

data class WeatherSuccessUiModel(
    val currentDateTime: Instant,
    val weatherPager: WeatherPagerUiModel
) : WeatherUiModel()

@Immutable
sealed interface IconSpec {
    class ImageVectorIcon(val imageVector: ImageVector) : IconSpec
    class ResourceIcon(val resourceFile: String) : IconSpec
}

@Immutable
data class WeatherPagerUiModel(
    val pages: List<WeatherPageUiModel>
)

@Immutable
data class WeatherPageUiModel(
    val placeName: String,
    val temperature: Int?,
    val feltTemperature: Int?,
    val windSpeed: Int?,
    val chanceOfPrecipitation: String,
    val humidityPercentage: String,
    val weatherDescription: String?,
    val backgroundColor: Color,
    val hourlyWeather: List<HourWeatherUiModel>,
    val weatherIcon: IconSpec?
)

@Immutable
data class HourWeatherUiModel(
    val time: LocalTime,
    val icon: IconSpec?,
    val temperature: Int?
)