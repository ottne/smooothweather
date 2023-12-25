package de.danotter.smooothweather.shared.di

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.danotter.smooothweather.shared.db.Database
import de.danotter.smooothweather.shared.db.PlaceQueries
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): Database {
        val driver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "test.db"
        )

        return Database(driver)
    }

    @Provides
    @Singleton
    fun providePlaceQueries(database: Database): PlaceQueries {
        return database.placeQueries
    }
}
