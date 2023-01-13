package de.danotter.smooothweather

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.danotter.smooothweather.domain.GetWeatherOverviewUseCase
import de.danotter.smooothweather.domain.WeatherType
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

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

                                val icon = weatherData.weatherType?.let(::getWeatherIcon)

                                val color = Color(color = Random.nextInt())
                                    .copy(alpha = 1f)
                                WeatherPageUiModel(
                                    placeName = weatherItem.placeName.orEmpty(),
                                    temperature = weatherData.temperature?.toInt(),
                                    weatherDescription = weatherData.weatherDescription,
                                    weatherIcon = icon,
                                    feltTemperature = weatherData.apparentTemperature?.toInt()?.let { "$it°" } ?:"?",
                                    chanceOfPrecipitation = weatherData.precipitation?.toString() ?: "? %",
                                    windSpeed = weatherData.windSpeed?.toInt()?.toString() ?: "?",
                                    humidityPercentage = weatherData.humidity?.toString() ?: "?",
                                    backgroundColor = color
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

sealed interface IconSpec {
    class ImageVectorIcon(val imageVector: ImageVector) : IconSpec
    class ResourceIcon(@DrawableRes val resourceId: Int) : IconSpec
}

data class WeatherPagerUiModel(
    val pages: List<WeatherPageUiModel>
)

data class WeatherPageUiModel(
    val placeName: String,
    val temperature: Int?,
    val feltTemperature: String,
    val windSpeed: String,
    val chanceOfPrecipitation: String,
    val humidityPercentage: String,
    val weatherDescription: String?,
    val backgroundColor: Color,
    val weatherIcon: IconSpec?
)
