package de.danotter.smooothweather.shared.domain

import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLGeocodeCompletionHandler
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.CLPlacemark
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// TODO upcoming in Kotlin 1.9.0
@RequiresOptIn
annotation class ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class GetCurrentPlaceUseCase {
    actual suspend operator fun invoke(): CurrentPlaceResult {
        val locationManager = CLLocationManager()
        val userLocation = locationManager.findUserLocationFast() ?: return CurrentPlaceFailureResult()
        val (lon, lat) = userLocation.coordinate.useContents {
            longitude to latitude
        }

        val geocoder = CLGeocoder()

        val geocoderResult: CLPlacemark = suspendCoroutine { cont ->
            geocoder.reverseGeocodeLocation(userLocation, object : CLGeocodeCompletionHandler {
                override fun invoke(placemark: List<*>?, error: NSError?) {
                    if (error != null || placemark.isNullOrEmpty()) {
                        cont.resume(null)
                    } else {
                        cont.resume(placemark.first() as CLPlacemark)
                    }
                }
            })
        } ?: return CurrentPlaceFailureResult()

        // TODO error handling
        return CurrentPlaceSuccessResult(
            locality = geocoderResult.name ?: "Unknown", // TODO grab this from somewhere
            longitude = lon,
            latitude = lat,
        )
    }

    private suspend fun CLLocationManager.findUserLocationFast(): CLLocation? {
        val location = this.location
        if (location != null) {
            return location
        }

        return suspendCancellableCoroutine { cont ->
            delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    val locations = didUpdateLocations as List<CLLocation>

                    this@findUserLocationFast.delegate = null
                    cont.resume(locations.firstOrNull())
                }
            }

            requestLocation()
        }
    }
}

