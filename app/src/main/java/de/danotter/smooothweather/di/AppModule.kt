package de.danotter.smooothweather.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.danotter.smooothweather.BuildConfig
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Named("DEBUG")
    fun provideIsDebug(): Boolean = BuildConfig.DEBUG
}