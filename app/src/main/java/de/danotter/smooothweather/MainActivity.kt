package de.danotter.smooothweather

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import de.danotter.smooothweather.ui.PlaceSelectionViewModel
import de.danotter.smooothweather.ui.SmooothWeatherApp
import de.danotter.smooothweather.ui.WeatherViewModel
import de.danotter.smooothweather.ui.theme.SmooothWeatherTheme
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val weatherViewModel by viewModels<WeatherViewModel>()

    private val placeSelectionViewModel by viewModels<PlaceSelectionViewModel>()

    private val locationPermissionGranted = MutableStateFlow(false)

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                locationPermissionGranted.value = true
            }
            else -> {
                // No location access granted.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )

        setContent {
            val locationPermissionGranted by locationPermissionGranted.collectAsState()

            SmooothWeatherTheme {
                if (locationPermissionGranted) {
                    val placeSelectionUiModel by placeSelectionViewModel.uiModel.collectAsState()
                    val weatherUiModel by weatherViewModel.uiModel.collectAsState()

                    SmooothWeatherApp(
                        weatherUiModel,
                        placeSelectionUiModel,
                        onQueryChange = placeSelectionViewModel::setQuery,
                        onSelectPlace = placeSelectionViewModel::selectPlace
                    )
                }
            }
        }
    }
}
