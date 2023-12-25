package de.danotter.smooothweather.shared.ui.weather

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import de.danotter.smooothweather.shared.domain.GetWeatherOverviewUseCase
import de.danotter.smooothweather.shared.domain.WeatherType
import de.danotter.smooothweather.shared.ui.BaseViewModel
import de.danotter.smooothweather.shared.ui.HourWeatherUiModel
import de.danotter.smooothweather.shared.ui.IconSpec
import de.danotter.smooothweather.shared.ui.WeatherErrorUiModel
import de.danotter.smooothweather.shared.ui.WeatherLoadingUiModel
import de.danotter.smooothweather.shared.ui.WeatherPageUiModel
import de.danotter.smooothweather.shared.ui.WeatherPagerUiModel
import de.danotter.smooothweather.shared.ui.WeatherSuccessUiModel
import de.danotter.smooothweather.shared.ui.WeatherUiModel
import de.danotter.smooothweather.shared.util.logError
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime

class WeatherViewModel(
    getWeatherOverview: GetWeatherOverviewUseCase,
    clock: Clock
) : BaseViewModel() {

    val uiModel: StateFlow<WeatherUiModel> = flow {
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
                                    chanceOfPrecipitation = weatherData.precipitation?.toString()
                                        ?: "? %",
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
            logError("Error loading weather data", error)

            emit(WeatherErrorUiModel)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            WeatherLoadingUiModel
        )

    private fun getWeatherIcon(weatherType: WeatherType): IconSpec {
        return when (weatherType) {
            WeatherType.CLOUDY -> IconSpec.ImageVectorIcon(Icons.Default.Cloud)
            WeatherType.PARTLY_CLOUDY -> IconSpec.ResourceIcon("ic_partly_cloudy.xml")
            WeatherType.CLEAR_SKY -> IconSpec.ImageVectorIcon(Icons.Default.WbSunny)
            WeatherType.FOG -> IconSpec.ImageVectorIcon(Icons.Default.Cloud)
            WeatherType.DRIZZLE -> IconSpec.ResourceIcon("ic_rainy.xml")
            WeatherType.RAIN -> IconSpec.ResourceIcon("ic_rainy.xml")
            WeatherType.SNOW -> IconSpec.ResourceIcon("ic_snowy.xml")
            WeatherType.STORM -> IconSpec.ResourceIcon("ic_thunderstorm.xml")
        }
    }
}
