package de.danotter.smooothweather

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.danotter.smooothweather.domain.GetWeatherOverviewUseCase
import de.danotter.smooothweather.domain.WeatherType
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    getWeatherOverview: GetWeatherOverviewUseCase,
    clock: Clock
) : ViewModel() {

    val uiModel: StateFlow<WeatherUiModel> = flow {
        emit(WeatherLoadingUiModel)

        emitAll(
            getWeatherOverview()
                .map { weatherOverview ->
                    WeatherSuccessUiModel(
                        clock.now(),
                        weatherPager = WeatherPagerUiModel(
                            pages = weatherOverview.items.map { weatherItem ->
                                val weatherData = weatherItem.weatherData

                                val temperature = weatherData.temperature?.toInt()
                                val icon = weatherData.weatherType?.let(::getWeatherIcon)

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
                                            icon = value.weatherType?.let { getWeatherIcon(it) },
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
            Timber.e(error, "Error loading weather data")

            emit(WeatherErrorUiModel)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), WeatherLoadingUiModel)

    private fun getWeatherIcon(weatherType: WeatherType): IconSpec {
        return when (weatherType) {
            WeatherType.CLOUDY -> IconSpec.ImageVectorIcon(Icons.Default.Cloud)
            WeatherType.PARTLY_CLOUDY -> IconSpec.ResourceIcon(R.drawable.ic_partly_cloudy)
            WeatherType.CLEAR_SKY -> IconSpec.ImageVectorIcon(Icons.Default.WbSunny)
            WeatherType.FOG -> IconSpec.ImageVectorIcon(Icons.Default.Cloud)
            WeatherType.DRIZZLE -> IconSpec.ResourceIcon(R.drawable.ic_rainy)
            WeatherType.RAIN -> IconSpec.ResourceIcon(R.drawable.ic_rainy)
            WeatherType.SNOW -> IconSpec.ResourceIcon(R.drawable.ic_snowy)
            WeatherType.STORM -> IconSpec.ResourceIcon(R.drawable.ic_thunderstorm)
        }
    }
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
    class ResourceIcon(@DrawableRes val resourceId: Int) : IconSpec
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
