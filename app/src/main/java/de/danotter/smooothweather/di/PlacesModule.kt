package de.danotter.smooothweather.di

import android.content.Context
import android.content.pm.PackageManager
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlacesModule {

    @Provides
    @PlacesApiKey
    fun providePlacesApiKey(
        @ApplicationContext context: Context
    ): String {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        val bundle = appInfo.metaData

        return requireNotNull(bundle.getString("com.google.android.geo.API_KEY")) { "Places API key not found" }
    }

    @Provides
    @Singleton
    fun providePlacesClient(
        @ApplicationContext context: Context,
        @PlacesApiKey placesApiKey: String
    ): PlacesClient {
        Places.initialize(
            context,
            placesApiKey
        )

        return Places.createClient(context)
    }
}


