package de.danotter.smooothweather.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock

@Module
@InstallIn(SingletonComponent::class)
class TimeModule {

    @Provides
    fun provideClock(): Clock {
        return Clock.System
    }
}
