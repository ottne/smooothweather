package de.danotter.smooothweather.shared.di

import android.location.Geocoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.danotter.smooothweather.shared.api.OpenMeteoApi
import de.danotter.smooothweather.shared.db.PlaceQueries
import de.danotter.smooothweather.shared.domain.AddPlaceUseCase
import de.danotter.smooothweather.shared.domain.GetCurrentPlaceUseCase
import de.danotter.smooothweather.shared.domain.GetPlaceTypeaheadUseCase
import de.danotter.smooothweather.shared.domain.GetSavedPlacesUseCase
import de.danotter.smooothweather.shared.domain.GetWeatherDataUseCase
import de.danotter.smooothweather.shared.domain.GetWeatherOverviewUseCase
import kotlinx.datetime.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DomainModule {

    @Provides
    @Singleton
    fun provideGetSavedPlaceUseCase(placeQueries: PlaceQueries): GetSavedPlacesUseCase {
        return GetSavedPlacesUseCase(
            placeQueries
        )
    }

    @Provides
    @Singleton
    fun provideAddPlaceUseCase(placeQueries: PlaceQueries): AddPlaceUseCase {
        return AddPlaceUseCase(
            placeQueries
        )
    }

    @Provides
    @Singleton
    fun provideGetPlaceTypeaheadUseCase(geocoder: Geocoder): GetPlaceTypeaheadUseCase {
        return GetPlaceTypeaheadUseCase(
            geocoder
        )
    }

    @Provides
    @Singleton
    fun provideWeatherDataUseCase(openMeteoApi: OpenMeteoApi): GetWeatherDataUseCase {
        return GetWeatherDataUseCase(
            openMeteoApi = openMeteoApi
        )
    }

    @Provides
    @Singleton
    fun getWeatherOverviewUseCase(
        getCurrentPlaceUseCase: GetCurrentPlaceUseCase,
        getSavedPlacesUseCase: GetSavedPlacesUseCase,
        getWeatherDataUseCase: GetWeatherDataUseCase,
        clock: Clock
    ): GetWeatherOverviewUseCase {
        return GetWeatherOverviewUseCase(
            getSavedPlaces = getSavedPlacesUseCase,
            getCurrentPlace = getCurrentPlaceUseCase,
            getWeatherData = getWeatherDataUseCase,
            clock = clock
        )
    }
}
