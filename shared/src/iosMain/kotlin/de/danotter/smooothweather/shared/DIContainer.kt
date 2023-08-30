package de.danotter.smooothweather.shared

import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import de.danotter.smooothweather.shared.api.OpenMeteoApi
import de.danotter.smooothweather.shared.db.Database
import de.danotter.smooothweather.shared.di.DIContainer
import de.danotter.smooothweather.shared.domain.AddPlaceUseCase
import de.danotter.smooothweather.shared.domain.GetCurrentPlaceUseCase
import de.danotter.smooothweather.shared.domain.GetPlaceTypeaheadUseCase
import de.danotter.smooothweather.shared.domain.GetSavedPlacesUseCase
import de.danotter.smooothweather.shared.domain.GetWeatherDataUseCase
import de.danotter.smooothweather.shared.domain.GetWeatherOverviewUseCase
import de.danotter.smooothweather.shared.ui.place.PlaceSelectionViewModel
import de.danotter.smooothweather.shared.ui.weather.WeatherViewModel
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import platform.Foundation.NSURLSession

object DefaultContainer : DIContainer {
    override val db by lazy { Database(NativeSqliteDriver(Database.Schema, "app.db")) }
    override val placeQueries by lazy { db.placeQueries }
    override val getCurrentPlace by lazy { GetCurrentPlaceUseCase() }
    override val getSavedPlaces by lazy {
        GetSavedPlacesUseCase(
            placeQueries
        )
    }
    override val addPlace by lazy { AddPlaceUseCase(placeQueries) }
    override val getPlaceTypeahead by lazy { GetPlaceTypeaheadUseCase() }
    override val json by lazy {
        Json {
            ignoreUnknownKeys = true
        }
    }
    override val openMeteoApi: OpenMeteoApi by lazy {
        OpenMeteoApi.create(
            baseUrl = "https://api.open-meteo.com/",
            urlSession = NSURLSession.sharedSession(),
            json = json
        )
    }
    override val getWeatherOverview by lazy {
        GetWeatherOverviewUseCase(
            getCurrentPlace,
            getWeatherData = getWeatherData,
            getSavedPlaces = getSavedPlaces,
            clock = Clock.System
        )
    }

    override val getWeatherData by lazy {
        GetWeatherDataUseCase(
            openMeteoApi
        )
    }

    override fun placeSelectionViewModel(): PlaceSelectionViewModel {
        return PlaceSelectionViewModel(
            getPlaceTypeahead,
            addPlace
        )
    }

    override fun weatherViewModel(): WeatherViewModel {
        return WeatherViewModel(
            getWeatherOverview,
            Clock.System
        )
    }
}
