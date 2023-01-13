@file:OptIn(ExperimentalFoundationApi::class)

package de.danotter.smooothweather

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import de.danotter.smooothweather.ui.PlaceSelectionViewModel
import de.danotter.smooothweather.ui.SmooothWeatherApp
import de.danotter.smooothweather.ui.theme.SmooothWeatherTheme
import java.util.*

@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val weatherViewModel by viewModels<WeatherViewModel>()

    private val placeSelectionViewModel by viewModels<PlaceSelectionViewModel>()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
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
            val placeSelectionUiModel by placeSelectionViewModel.uiModel.collectAsState()
            val weatherUiModel by weatherViewModel.uiModel.collectAsState()

            SmooothWeatherTheme {
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
