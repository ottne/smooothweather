package de.danotter.smooothweather.shared.api

import retrofit2.http.GET
import retrofit2.http.Query

actual interface OpenMeteoApi {

    @GET("/v1/forecast")
    actual suspend fun getWeather(
        @Query("longitude") longitude: Double,
        @Query("latitude") latitude: Double,
        @Query("hourly", encoded = true) hourlyValues: String
    ): MeteoWeatherResponse
}