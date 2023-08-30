package de.danotter.smooothweather

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import de.danotter.smooothweather.shared.di.DIContainer
import de.danotter.smooothweather.shared.ui.LocalViewModelStore
import de.danotter.smooothweather.shared.ui.SmoooothWeatherApp
import de.danotter.smooothweather.shared.ui.SmooothWeatherTheme
import de.danotter.smooothweather.shared.ui.ViewModelStore
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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

    private lateinit var configStore: ViewModelStore

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onRetainCustomNonConfigurationInstance(): ViewModelStore {
        return configStore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        @Suppress("DEPRECATION")
        configStore = lastCustomNonConfigurationInstance as? ViewModelStore ?: ViewModelStore()

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )

        val diContainer = EntryPointAccessors.fromApplication<HiltDIContainer>(applicationContext)

        setContent {
            val locationPermissionGranted by locationPermissionGranted.collectAsState()

            SmooothWeatherTheme {
                CompositionLocalProvider(LocalViewModelStore provides configStore) {
                    if (locationPermissionGranted) {
                        SmoooothWeatherApp(diContainer = diContainer)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            configStore.dispose()
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltDIContainer : DIContainer
