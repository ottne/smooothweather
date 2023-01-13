package de.danotter.smooothweather.di

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.danotter.smooothweather.Database
import de.danotter.smooothweather.db.PlaceQueries
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): Database {
        val driver = AndroidSqliteDriver(Database.Schema, context, "test.db")

        return Database(driver)
    }

    @Provides
    @Singleton
    fun providePlaceQueries(database: Database): PlaceQueries {
        return database.placeQueries
    }
}
