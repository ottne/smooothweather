@file:OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
)

package de.danotter.smooothweather.shared.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import de.danotter.smooothweather.*
import de.danotter.smooothweather.shared.di.DIContainer
import de.danotter.smooothweather.shared.ui.pagerindicator.HorizontalPagerIndicator
import de.danotter.smooothweather.shared.ui.place.PlaceSelectionViewModel
import de.danotter.smooothweather.shared.ui.weather.WeatherViewModel
import kotlinx.datetime.*
import kotlin.math.abs

@Composable
fun SmoooothWeatherApp(diContainer: DIContainer) {

    val weatherViewModel = viewModel(WeatherViewModel::class, diContainer::weatherViewModel)
    val placeSelectionViewModel = viewModel(PlaceSelectionViewModel::class, diContainer::placeSelectionViewModel)

    val weatherUiModel by weatherViewModel.uiModel.collectAsState()
    val placeSelectionUiModel by placeSelectionViewModel.uiModel.collectAsState()

    SmooothWeatherApp(
        weatherUiModel,
        placeSelectionUiModel,
        onQueryChange = placeSelectionViewModel::setQuery,
        onSelectPlace = placeSelectionViewModel::selectPlace
    )
}

@Composable
internal fun SmooothWeatherApp(
    weatherUiModel: WeatherUiModel,
    placeSelectionUiModel: PlaceSelectionUiModel,
    onQueryChange: (String) -> Unit,
    onSelectPlace: (String) -> Unit
) {
    val dialogState = remember { DialogState() }

    PlaceSelectionDialog(
        dialogState = dialogState,
        uiModel = placeSelectionUiModel,
        onQueryChange = onQueryChange,
        onSelectPlace = onSelectPlace
    )

    WeatherScreen(
        weatherUiModel,
        onOpenPlaceSelection = {
            dialogState.show()
        }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeatherScreen(
    uiModel: WeatherUiModel,
    onOpenPlaceSelection: () -> Unit
) {
    val pagerState = rememberPagerState(
        pageCount = { (uiModel as? WeatherSuccessUiModel)?.weatherPager?.pages?.size ?: 0 },
        initialPage = 0
    )

    val backgroundColor = if (uiModel is WeatherSuccessUiModel) {
        val pages = uiModel.weatherPager.pages
        if (pages.isNotEmpty()) {
            uiModel.weatherPager.pages[pagerState.currentPage].backgroundColor
        } else {
            MaterialTheme.colorScheme.primary
        }
    } else {
        MaterialTheme.colorScheme.primary
    }
    val animatedBackgroundColor by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Scaffold(
        containerColor = animatedBackgroundColor,
        contentColor = Color.White,
        topBar = {
            SmoothWeatherMainAppBar(uiModel, pagerState.currentPage, onOpenPlaceSelection)
        }
    ) { contentPadding ->
        when (uiModel) {
            WeatherErrorUiModel -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize()
                ) {
                    Text(
                        text = "Error loading weather data.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            WeatherLoadingUiModel -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        color = LocalContentColor.current
                    )
                }
            }
            is WeatherSuccessUiModel -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize()
                ) {
                   WeatherPager(
                       uiModel,
                       pagerState
                   )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeatherPager(
    uiModel: WeatherSuccessUiModel,
    pagerState: PagerState
) {
    Column {
        val currentPage = uiModel.weatherPager.pages[pagerState.currentPage]

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            TemperatureMainView(
                currentPage
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            val page = uiModel.weatherPager.pages[index]
            WeatherPage(page = page)
        }

        if (uiModel.weatherPager.pages.size > 1) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        pageCount = uiModel.weatherPager.pages.size,
                        modifier = Modifier
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TemperatureMainView(
    page: WeatherPageUiModel
) {
    val transition = updateTransition(page, "pageTransition")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        val animatedTemperature by animateTemperatureAsState(page.temperature ?: 0)

        Text(
            text = buildAnnotatedString {
                append("$animatedTemperature°")
                this.withStyle(
                    SpanStyle(
                        baselineShift = BaselineShift.Superscript,
                        fontSize = 54.sp
                    )
                ) {
                    append("C")
                }
            },
            style = MaterialTheme.typography.displayLarge
                .copy(fontSize = 144.sp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            transition.PageTransition { page ->
                if (page.weatherIcon != null) {
                    Icon(
                        iconSpecPainter(iconSpec = page.weatherIcon),
                        contentDescription = null,
                        modifier = Modifier.size(width = 48.dp, height = 48.dp)
                    )
                }
            }

            transition.PageTransition { page ->
                Text(
                    page.weatherDescription ?: "",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmoothWeatherMainAppBar(
    uiModel: WeatherUiModel,
    currentPageIndex: Int,
    onOpenPlaceSelection: () -> Unit
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = {
            if (uiModel is WeatherSuccessUiModel && uiModel.weatherPager.pages.isNotEmpty()) {
                val currentPage = uiModel.weatherPager.pages[currentPageIndex]
                MainTitle(
                    title = currentPage.placeName,
                    subtitle = formatDateTime(uiModel.currentDateTime),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        actions = {
            IconButton(onClick = onOpenPlaceSelection) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add location")
            }
        }
    )
}

@Composable
private fun WeatherPage(
    page: WeatherPageUiModel
) {
    Column {
        Spacer(modifier = Modifier.weight(1f))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (page.hourlyWeather.isNotEmpty()) {
                HourlyWeatherRow(
                    hourlyData = HourlyListWrapper(
                        value = page.hourlyWeather
                    ),
                    timeFormatter = { time ->
                        formatHourlyTime(time)
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data available")
                }
            }
        }

        WeatherDetailCard(
            feltTemperature = "${page.feltTemperature}° C",
            windSpeed = "${page.windSpeed} km/h",
            chanceOfPrecipitation = page.chanceOfPrecipitation,
            humidityPercentage = page.humidityPercentage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

// todo use kotlinx immutable lists
@Immutable
private data class HourlyListWrapper(
    val value: List<HourWeatherUiModel>
)

@Composable
private fun HourlyWeatherRow(
    hourlyData: HourlyListWrapper,
    timeFormatter: @Composable (time: LocalTime) -> String,
) {
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = 12
    )

    LazyRow(
        state = lazyListState,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(hourlyData.value) { hourlyWeather ->
            HourlyWeatherItem(
                hour = {
                    Text(
                        timeFormatter(hourlyWeather.time),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                icon = {
                    if (hourlyWeather.icon != null) {
                        Icon(
                            iconSpecPainter(hourlyWeather.icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                temperature = {
                    Text(hourlyWeather.temperature?.let { "${it}° C" } ?: "?")
                }
            )
        }
    }
}

@Composable
fun HourlyWeatherItem(
    hour: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    temperature: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        hour()
        Spacer(modifier = Modifier.height(4.dp))
        icon()
        temperature()
    }
}

@Composable
private fun WeatherDetailCard(
    feltTemperature: String,
    windSpeed: String,
    chanceOfPrecipitation: String,
    humidityPercentage: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Weather now", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ){
                WeatherDataItem(
                    title = "Feels like",
                    content = feltTemperature,
                    icon = { Icon(Icons.Default.Thermostat, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )

                WeatherDataItem(
                    title = "Wind speed",
                    content = windSpeed,
                    icon = { Icon(Icons.Default.Air, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                WeatherDataItem(
                    title = "Precipitation",
                    content = chanceOfPrecipitation,
                    icon = { Icon(Icons.Default.Umbrella, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )

                WeatherDataItem(
                    title = "Humidity",
                    content = humidityPercentage,
                    icon = { Icon(Icons.Default.WaterDrop, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WeatherDataItem(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(12.dp))
        }

        Column {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
                Text(title)
            }

            CompositionLocalProvider {
                Text(content)
            }
        }
    }
}

@Composable
private fun iconSpecPainter(iconSpec: IconSpec): Painter {
    return when (iconSpec) {
        is IconSpec.ImageVectorIcon -> {
            rememberVectorPainter(image = iconSpec.imageVector)
        }
        is IconSpec.ResourceIcon -> {
            resourceIdPainter(resourceId = iconSpec.resourceId)
        }
    }
}

@Suppress("NAME_SHADOWING")
@Composable
private fun MainTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    DefaultAnimatedContent(
        targetState = title to subtitle
    ) { titleState ->
        val (title, subtitle) = titleState
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.height(28.dp)
            )

            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun <S> DefaultAnimatedContent(
    targetState: S,
    content: @Composable AnimatedVisibilityScope.(targetState: S) -> Unit
) {
    AnimatedContent(
        targetState,
        transitionSpec = {
            val spring = spring<Float>(stiffness = Spring.StiffnessLow)
            val intSpring = spring<IntOffset>(stiffness = Spring.StiffnessLow)
            (slideInVertically(animationSpec = intSpring) { height -> height } + fadeIn(spring) togetherWith
                    slideOutVertically(animationSpec = intSpring) { height -> -height } + fadeOut(spring)) using SizeTransform(clip = false)
        },
        content = content
    )
}

@Composable
private fun Transition<WeatherPageUiModel>.PageTransition(
    content: @Composable AnimatedVisibilityScope.(targetState: WeatherPageUiModel) -> Unit
) {
    AnimatedContent(
        transitionSpec = {
            val spring = spring<Float>(stiffness = Spring.StiffnessLow)
            val intSpring = spring<IntOffset>(stiffness = Spring.StiffnessLow)
            (slideInVertically(animationSpec = intSpring) { height -> height } + fadeIn(spring) with
                    slideOutVertically(animationSpec = intSpring) { height -> -height } + fadeOut(spring))
                .using(
                    SizeTransform(clip = false)
                )
        },
        content = content
    )
}

/**
 * Makes animating number in text ui look somewhat more natural.
 */
@Composable
private fun animateTemperatureAsState(
    number: Int
): State<Int> {
    var previousNumber by remember { mutableStateOf(number) }

    val animatableNumber = remember { Animatable(number, Int.VectorConverter) }
    LaunchedEffect(number) {
        if (previousNumber == number) {
            return@LaunchedEffect
        }

        val savedPrevious = previousNumber
        previousNumber = number

        animatableNumber.animateTo(
            number,
            animationSpec = keyframes {
                durationMillis = 375

                if (abs(savedPrevious - number) > 3) {
                    if (savedPrevious < number) {
                        number - 3 at 150 with FastOutSlowInEasing
                    } else {
                        number + 3 at 150 with FastOutSlowInEasing
                    }
                }

                number at 375 with LinearEasing
            }
        )
    }

    return animatableNumber.asState()
}
