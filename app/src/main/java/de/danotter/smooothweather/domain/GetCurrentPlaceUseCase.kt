package de.danotter.smooothweather.domain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.qualifiers.ApplicationContext
import de.danotter.smooothweather.util.getFromLocationAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class GetCurrentPlaceUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geocoder: Geocoder,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) {

    suspend operator fun invoke(): CurrentPlaceResult {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val lastLocation = fusedLocationProviderClient.lastLocation.await()

            val addresses = geocoder.getFromLocationAsync(lastLocation.latitude, lastLocation.longitude, 1)

            val address = addresses.firstOrNull() ?: return CurrentPlaceFailureResult(null)
            return CurrentPlaceSuccessResult(
                longitude = address.longitude,
                latitude = address.latitude,
                locality = address.locality
            )
        } else {
            return CurrentPlacePermissionNotGranted
        }
    }
}

sealed interface CurrentPlaceResult

data class CurrentPlaceSuccessResult(
    val locality: String?,
    val longitude: Double?,
    val latitude: Double?
) : CurrentPlaceResult

object CurrentPlacePermissionNotGranted : CurrentPlaceResult

class CurrentPlaceFailureResult(error: Throwable? = null, val message: String? = null) : CurrentPlaceResult
