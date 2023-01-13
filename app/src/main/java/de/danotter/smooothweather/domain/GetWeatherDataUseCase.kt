package de.danotter.smooothweather.domain

import de.danotter.smooothweather.api.OpenMeteoApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.random.nextInt

@Singleton
class GetWeatherDataUseCase @Inject constructor(
    private val openMeteoApi: OpenMeteoApi
) {

    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
    ): WeatherData {
        val weather = openMeteoApi.getWeather(
            latitude = latitude,
            longitude = longitude,
            hourlyValues = listOf(
                OpenMeteoApi.HourlyValues.TEMPERATURE_2M,
                OpenMeteoApi.HourlyValues.RAIN,
                OpenMeteoApi.HourlyValues.WINDSPEED_10M,
                OpenMeteoApi.HourlyValues.RELATIVE_HUMIDITY_10m,
                OpenMeteoApi.HourlyValues.APPARENT_TEMPERATURE,
                OpenMeteoApi.HourlyValues.WEATHERCODE
            )
                .map(OpenMeteoApi.HourlyValues::paramName)
                .joinToString(",")
        )

        val temperature = weather.hourly?.temperature_2m?.firstOrNull()
        val apparentTemperature = weather.hourly?.apparent_temperature?.firstOrNull()

        val (weatherDescription, weatherType) = when (weather.hourly?.weathercode?.firstOrNull()) {
            0 -> "Clear sky" to WeatherType.CLEAR_SKY
            1, 2, 3 -> "Mainly clear" to WeatherType.CLOUDY
            45, 48 -> "Fog" to WeatherType.FOG
            51, 53, 55 -> "Drizzle" to WeatherType.DRIZZLE
            56, 57 -> "Freezing Drizzle" to WeatherType.DRIZZLE
            61, 63, 65 -> "Rain" to WeatherType.RAIN
            66, 67 -> "Freezing Rain" to WeatherType.RAIN
            71, 73, 75 -> "Snow fall" to WeatherType.SNOW
            77 -> "Snow grains" to WeatherType.SNOW
            80, 81, 82 -> "Rain showers" to WeatherType.RAIN
            85, 86 -> "Snow showers" to WeatherType.SNOW
            95 -> "Thunderstorm" to WeatherType.STORM
            96, 99 -> "Thunderstorm" to WeatherType.STORM
            else -> null to null
        }

        return WeatherData(
            temperature = temperature,
            weatherDescription = weatherDescription,
            weatherType = weatherType,
            apparentTemperature = apparentTemperature,
            // apparently OpenMeteo's API does not give you precipitation data, so just use some random value instead
            precipitation = Random.Default.nextInt(0..100),
            windSpeed = weather.hourly?.windspeed_10m?.firstOrNull(),
            humidity = weather.hourly?.relativehumidity_2m?.firstOrNull(),
        )
    }
}

enum class WeatherType {
    CLOUDY,
    CLEAR_SKY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    STORM
}

sealed interface WeatherDataResult

data class WeatherData(
    val temperature: Double?,
    val weatherDescription: String?,
    val apparentTemperature: Double?,
    val windSpeed: Double?,
    val humidity: Int?,
    val weatherType: WeatherType?,
    val precipitation: Int?
)
