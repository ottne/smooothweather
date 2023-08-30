package de.danotter.smooothweather.shared.api

import kotlinx.serialization.Serializable

expect interface OpenMeteoApi {
    suspend fun getWeather(
        longitude: Double,
        latitude: Double,
        hourlyValues: String
    ): MeteoWeatherResponse
}

enum class HourlyValues(val paramName: String) {
    TEMPERATURE_2M("temperature_2m"),
    RAIN("rain"),
    WINDSPEED_10M("windspeed_10m"),
    RELATIVE_HUMIDITY_10M("relativehumidity_2m"),
    APPARENT_TEMPERATURE("apparent_temperature"),
    WEATHERCODE("weathercode")
}

@Serializable
data class MeteoWeatherResponse(
    val longitude: Double,
    val latitude: Double,
    val hourly: HourlyData?
)

@Serializable
data class HourlyData(
    val time: List<String>,
    val temperature_2m: List<Double>? = null,
    val weathercode: List<Int>? = null,
    val apparent_temperature: List<Double>? = null,
    val windspeed_10m: List<Double>? = null,
    val relativehumidity_2m: List<Int>? = null
)

/*

WMO Weather interpretation codes (WW)

0	Clear sky
1, 2, 3	Mainly clear, partly cloudy, and overcast
45, 48	Fog and depositing rime fog
51, 53, 55	Drizzle: Light, moderate, and dense intensity
56, 57	Freezing Drizzle: Light and dense intensity
61, 63, 65	Rain: Slight, moderate and heavy intensity
66, 67	Freezing Rain: Light and heavy intensity
71, 73, 75	Snow fall: Slight, moderate, and heavy intensity
77	Snow grains
80, 81, 82	Rain showers: Slight, moderate, and violent
85, 86	Snow showers slight and heavy
95 	Thunderstorm: Slight or moderate
96, 99 	Thunderstorm with slight and heavy hail
 */
