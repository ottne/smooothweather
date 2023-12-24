@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package de.danotter.smooothweather.shared.domain

import kotlinx.cinterop.useContents
import platform.CoreLocation.CLGeocodeCompletionHandler
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLPlacemark
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class GetPlaceTypeaheadUseCase {
    actual suspend operator fun invoke(query: String): PlaceTypeaheadResult {
        val geocoder = CLGeocoder()

        val placemarks: List<CLPlacemark> = geocoder.geocodeAddressStringAsync(query)

        return PlaceTypeheadSuccessResult(
            items = placemarks.mapIndexedNotNull { index: Int, placemarkItem: CLPlacemark ->
                placemarkItem.toPlaceTypeaheadItem(index.toString())
            }
        )
    }
}

private fun CLPlacemark.toPlaceTypeaheadItem(id: String): PlaceTypeaheadItem? {
    val (lon, lat) = this.location?.coordinate?.useContents {
        longitude to latitude
    } ?: return null

    return PlaceTypeaheadItem(
        id = id,
        name = name ?: "",
        latitude = lat,
        longitude = lon
    )
}

private suspend fun CLGeocoder.geocodeAddressStringAsync(query: String): List<CLPlacemark> {
    return suspendCoroutine { cont ->
        geocodeAddressString(
            addressString = query,
            completionHandler = object : CLGeocodeCompletionHandler {
                override fun invoke(placemark: List<*>?, error: NSError?) {
                    if (error != null || placemark.isNullOrEmpty()) {
                        cont.resume(emptyList())
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        cont.resume(placemark as List<CLPlacemark>)
                    }
                }
            }
        )
    }
}
