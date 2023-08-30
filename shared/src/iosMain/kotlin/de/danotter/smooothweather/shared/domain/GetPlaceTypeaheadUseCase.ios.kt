package de.danotter.smooothweather.shared.domain

import kotlinx.cinterop.useContents
import platform.CoreLocation.CLGeocodeCompletionHandler
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLPlacemark
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
actual class GetPlaceTypeaheadUseCase {
    actual suspend operator fun invoke(query: String): PlaceTypeaheadResult {
        val geocoder = CLGeocoder()

        val placemark: List<CLPlacemark> = suspendCoroutine { cont ->
            geocoder.geocodeAddressString(
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

        return PlaceTypeheadSuccessResult(
            items = placemark.mapIndexedNotNull { index, placemarkItem ->
                val (lon, lat) = placemarkItem.location?.coordinate?.useContents {
                    longitude to latitude
                } ?: return@mapIndexedNotNull null

                PlaceTypeaheadItem(
                    id = index.toString(),
                    name = placemarkItem.name ?: "",
                    latitude = lat,
                    longitude = lon
                )
            }
        )
    }
}
