package de.danotter.smooothweather.shared.di

import de.danotter.smooothweather.shared.api.OpenMeteoApi
import de.danotter.smooothweather.shared.db.Database
import de.danotter.smooothweather.shared.db.PlaceQueries
import de.danotter.smooothweather.shared.domain.AddPlaceUseCase
import de.danotter.smooothweather.shared.domain.GetCurrentPlaceUseCase
import de.danotter.smooothweather.shared.domain.GetPlaceTypeaheadUseCase
import de.danotter.smooothweather.shared.domain.GetSavedPlacesUseCase
import de.danotter.smooothweather.shared.domain.GetWeatherDataUseCase
import de.danotter.smooothweather.shared.domain.GetWeatherOverviewUseCase
import de.danotter.smooothweather.shared.ui.place.PlaceSelectionViewModel
import de.danotter.smooothweather.shared.ui.weather.WeatherViewModel
import kotlinx.serialization.json.Json

interface DIContainer {
    val db: Database
    val placeQueries: PlaceQueries
    val getCurrentPlace: GetCurrentPlaceUseCase
    val getSavedPlaces: GetSavedPlacesUseCase
    val addPlace: AddPlaceUseCase
    val getPlaceTypeahead: GetPlaceTypeaheadUseCase
    val json: Json
    val openMeteoApi: OpenMeteoApi
    val getWeatherData: GetWeatherDataUseCase
    val getWeatherOverview: GetWeatherOverviewUseCase
    fun placeSelectionViewModel(): PlaceSelectionViewModel
    fun weatherViewModel(): WeatherViewModel
}