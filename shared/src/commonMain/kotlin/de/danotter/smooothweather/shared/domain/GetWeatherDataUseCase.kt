package de.danotter.smooothweather.shared.domain

import de.danotter.smooothweather.shared.api.HourlyValues
import de.danotter.smooothweather.shared.api.OpenMeteoApi
import kotlinx.datetime.LocalTime
import kotlin.random.Random
import kotlin.random.nextInt

class GetWeatherDataUseCase constructor(
    private val openMeteoApi: OpenMeteoApi
) {

    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        currentTime: LocalTime
    ): WeatherData {
        val openMeteoData = openMeteoApi.getWeather(
            latitude = latitude,
            longitude = longitude,
            hourlyValues = listOf(
                HourlyValues.TEMPERATURE_2M,
                HourlyValues.RAIN,
                HourlyValues.WINDSPEED_10M,
                HourlyValues.RELATIVE_HUMIDITY_10M,
                HourlyValues.APPARENT_TEMPERATURE,
                HourlyValues.WEATHERCODE
            )
                .map(HourlyValues::paramName)
                .joinToString(",")
        )

        val hourlyIndex = currentTime.hour

        val hourlyData = openMeteoData.hourly
        val temperature = hourlyData?.temperature_2m?.getOrNull(hourlyIndex)
        val apparentTemperature = hourlyData?.apparent_temperature?.getOrNull(hourlyIndex)

        val (weatherDescription, weatherType) = getAdditionalDataForWeatherCode(
            hourlyData?.weathercode?.getOrNull(hourlyIndex)
        )

        return WeatherData(
            temperature = temperature,
            weatherDescription = weatherDescription,
            weatherType = weatherType,
            apparentTemperature = apparentTemperature,
            // apparently OpenMeteo's API does not give any precipitation data, so use a random value instead
            precipitation = Random.nextInt(0..100),
            windSpeed = hourlyData?.windspeed_10m?.getOrNull(hourlyIndex),
            humidity = hourlyData?.relativehumidity_2m?.getOrNull(hourlyIndex),
            hourlyWeather = List(24) { i ->
                WeatherByHour(
                    hourlyData?.temperature_2m?.getOrNull(i),
                    hourlyData?.weathercode?.getOrNull(i)?.let { getAdditionalDataForWeatherCode(it) }?.second
                )
            }
        )
    }

    private fun getAdditionalDataForWeatherCode(weatherCode: Int?): Pair<String?, WeatherType?> {
        return when (weatherCode) {
            0 -> "Clear sky" to WeatherType.CLEAR_SKY
            1, 2, 3 -> "Mainly clear" to WeatherType.PARTLY_CLOUDY
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
    }
}

enum class WeatherType {
    CLOUDY,
    PARTLY_CLOUDY,
    CLEAR_SKY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    STORM
}

data class WeatherData(
    val temperature: Double?,
    val weatherDescription: String?,
    val apparentTemperature: Double?,
    val windSpeed: Double?,
    val humidity: Int?,
    val weatherType: WeatherType?,
    val precipitation: Int?,
    val hourlyWeather: List<WeatherByHour>
)

data class WeatherByHour(
    val temperature: Double?,
    val weatherType: WeatherType?,
)

private fun <T> List<T>.getOrNull(index: Int): T? {
    require(index >= 0) { "Index must be non negative." }

    return if (index < size) this[index] else null
}
