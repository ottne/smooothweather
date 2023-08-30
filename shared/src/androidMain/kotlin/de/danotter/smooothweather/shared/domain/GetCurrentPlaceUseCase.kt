package de.danotter.smooothweather.shared.domain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import de.danotter.smooothweather.shared.util.getFromLocationAsync
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
actual class GetCurrentPlaceUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geocoder: Geocoder,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) {

    actual suspend operator fun invoke(): CurrentPlaceResult {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return CurrentPlacePermissionNotGranted
        }

        val lastLocation = try {
            fusedLocationProviderClient.lastLocation.await() ?:
                fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_LOW_POWER, null).await()
        } catch (e: Exception) {
            null
        } ?: return CurrentPlaceFailureResult(
            NullPointerException("No last location available.")
        )

        val addresses = geocoder.getFromLocationAsync(lastLocation.latitude, lastLocation.longitude, 1)

        val address = addresses.firstOrNull() ?: return CurrentPlaceFailureResult()
        return CurrentPlaceSuccessResult(
            longitude = address.longitude,
            latitude = address.latitude,
            locality = address.locality
        )
    }
}