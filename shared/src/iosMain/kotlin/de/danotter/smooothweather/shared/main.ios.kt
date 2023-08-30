package de.danotter.smooothweather.shared

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import de.danotter.smooothweather.shared.di.DIContainer
import de.danotter.smooothweather.shared.ui.LocalViewModelStore
import de.danotter.smooothweather.shared.ui.SmoooothWeatherApp
import de.danotter.smooothweather.shared.ui.SmooothWeatherTheme
import de.danotter.smooothweather.shared.ui.ViewModelStore
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorized
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.darwin.NSObject

val container: DIContainer = DefaultContainer

fun MainViewController() = ComposeUIViewController {

    var locationAccessGranted: Boolean by remember { mutableStateOf(false) }

    val locationManager = remember {
        CLLocationManager().apply {
            delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                    val validStates = listOf(
                        kCLAuthorizationStatusAuthorized,
                        kCLAuthorizationStatusAuthorizedAlways,
                        kCLAuthorizationStatusAuthorizedWhenInUse
                    )
                    locationAccessGranted = validStates.contains(manager.authorizationStatus)
                }
            }
        }
    }

    LaunchedEffect(locationManager) {
        locationManager.requestWhenInUseAuthorization()
    }

    val viewModelStore = remember { ViewModelStore() }
    CompositionLocalProvider(LocalViewModelStore provides viewModelStore) {
        SmooothWeatherTheme {
            if (locationAccessGranted) {
                SmoooothWeatherApp(container)
            }
        }
    }
}
