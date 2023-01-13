package de.danotter.smooothweather.util

import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun Geocoder.getFromLocationAsync(
    latitude: Double,
    longitude: Double,
    maxResults: Int
): List<Address> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCoroutine { continuation ->
            getFromLocation(latitude, longitude, maxResults) { result ->
                continuation.resume(result)
            }
        }
    } else {
        withContext(Dispatchers.IO) {
            getFromLocation(latitude, longitude, maxResults).orEmpty()
        }
    }
}

suspend fun Geocoder.getFromLocationNameAsync(
    locationName: String,
    maxResults: Int
): List<Address> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCoroutine { continuation ->
            getFromLocationName(locationName, maxResults) { result ->
                continuation.resume(result)
            }
        }
    } else {
        withContext(Dispatchers.IO) {
            getFromLocationName(locationName, maxResults).orEmpty()
        }
    }
}
