package de.danotter.smooothweather.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.danotter.smooothweather.shared.domain.AddPlaceUseCase
import de.danotter.smooothweather.shared.domain.GetPlaceTypeaheadUseCase
import de.danotter.smooothweather.shared.domain.GetWeatherOverviewUseCase
import de.danotter.smooothweather.shared.ui.place.PlaceSelectionViewModel
import de.danotter.smooothweather.shared.ui.weather.WeatherViewModel
import kotlinx.datetime.Clock

@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {
    @Provides
    fun provideWeatherViewModel(
        getWeatherOverview: GetWeatherOverviewUseCase,
        clock: Clock
    ): WeatherViewModel {
        return WeatherViewModel(getWeatherOverview, clock)
    }

    @Provides
    fun providePlaceSelectionViewModel(
        getPlaceTypeahead: GetPlaceTypeaheadUseCase,
        addPlace: AddPlaceUseCase
    ): PlaceSelectionViewModel {
        return PlaceSelectionViewModel(
            getPlaceTypeahead,
            addPlace
        )
    }
}