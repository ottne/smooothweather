package de.danotter.smooothweather.ui

import android.text.format.DateFormat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import de.danotter.smooothweather.*
import de.danotter.smooothweather.ui.pagerindicator.HorizontalPagerIndicator
import de.danotter.smooothweather.ui.theme.SmooothWeatherTheme
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun SmooothWeatherApp(
    mainUiModel: WeatherUiModel,
    placeSelectionUiModel: PlaceSelectionUiModel,
    onQueryChange: (String) -> Unit,
    onSelectPlace: (String) -> Unit
) {
    var isShowDialog by rememberSaveable { mutableStateOf(false) }
    val placeSelectionTransitionState = remember {
        MutableTransitionState(initialState = isShowDialog)
    }

    isShowDialog = placeSelectionTransitionState.targetState

    PlaceSelectionDialog(
        transitionState = placeSelectionTransitionState,
        uiModel = placeSelectionUiModel,
        onQueryChange = onQueryChange,
        onSelectPlace = onSelectPlace
    )

    MainScreen(
        mainUiModel,
        onOpenPlaceSelection = {
            placeSelectionTransitionState.targetState = true
        }
    )
}


@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun MainScreen(
    uiModel: WeatherUiModel,
    onOpenPlaceSelection: () -> Unit
) {
    val pagerState = rememberPagerState()

    val backgroundColor = if (uiModel is WeatherSuccessUiModel) {
        uiModel.weatherPager.pages[pagerState.currentPage].backgroundColor
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

@ExperimentalFoundationApi
@Composable
private fun WeatherPager(
    uiModel: WeatherSuccessUiModel,
    pagerState: PagerState
) {
    Column {
        AnimatedTemperature(temperatureProducer = {
            uiModel.weatherPager.pages[pagerState.currentPage]
                .temperature ?: 0
        })

        HorizontalPager(
            pageCount = uiModel.weatherPager.pages.size,
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
private fun AnimatedTemperature(
    temperatureProducer: @Composable () -> Int
) {
    val tweenSpec = tween<Int>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
    val springSpec = spring<Int>()
    val animatedTemperature by animateIntAsState(
        targetValue = temperatureProducer(),
        springSpec
    )
    //Timber.i("targetvalue=$temperature,animated=$animatedTemperature")
    Text(
        text = "$animatedTemperature°",
        style = MaterialTheme.typography.displayLarge
            .copy(fontSize = 96.sp),
    )
}

@ExperimentalMaterial3Api
@Composable
private fun SmoothWeatherMainAppBar(
    uiModel: WeatherUiModel,
    currentPageIndex: Int,
    onOpenPlaceSelection: () -> Unit
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = {
            if (uiModel is WeatherSuccessUiModel) {
                val locale = LocaleListCompat.getAdjustedDefault()[0]
                val dateTimeFormatted = remember(locale) {
                    val pattern = DateFormat.getBestDateTimePattern(
                        locale,
                        "ddMMMMhhmm"
                    )
                    val format = SimpleDateFormat(pattern, locale)
                    val date = Date.from(uiModel.currentDateTime.toJavaInstant())
                    format.format(date)
                }

                val currentPage = uiModel.weatherPager.pages[currentPageIndex]
                MainTitle(
                    title = currentPage.placeName,
                    subtitle = dateTimeFormatted
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    page.weatherDescription ?: "",
                    style = MaterialTheme.typography.titleMedium
                )

                if (page.weatherIcon != null) {
                    Icon(iconSpecPainter(iconSpec = page.weatherIcon), contentDescription = null)
                }
            }

            Text(
                text = "${page.temperature}°",
                style = MaterialTheme.typography.displayLarge
                    .copy(fontSize = 96.sp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        WeatherDetailCard(
            feltTemperature = page.feltTemperature,
            windSpeed = page.windSpeed,
            chanceOfPrecipitation = page.chanceOfPrecipitation,
            humidityPercentage = page.humidityPercentage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
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
                    title = "Wind",
                    content = windSpeed,
                    icon = { Icon(Icons.Default.Air, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth()
            ){
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
            painterResource(id = iconSpec.resourceId)
        }
    }
}

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MainTitle(
    title: String,
    subtitle: String,
) {
    AnimatedContent(
        targetState = title to subtitle,
        transitionSpec = {
            val spring = spring<Float>(stiffness = Spring.StiffnessLow)
            val intSpring = spring<IntOffset>(stiffness = Spring.StiffnessLow)
            (slideInVertically(animationSpec = intSpring) { height -> height } + fadeIn(spring) with
                    slideOutVertically(animationSpec = intSpring) { height -> -height } + fadeOut(spring))
                .using(
                    // Disable clipping since the faded slide-in/out should
                    // be displayed out of bounds.
                    SizeTransform(clip = false)
                )
        }
    ) { titleState ->
        val (title, subtitle) = titleState
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
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

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
@Preview
fun MainScreenPreview() {
    SmooothWeatherTheme {
        MainScreen(
            uiModel = WeatherSuccessUiModel(
                currentDateTime = LocalDateTime(
                    year = 2022,
                    monthNumber = 1,
                    dayOfMonth = 8,
                    hour = 12,
                    minute = 34,
                ).toInstant(TimeZone.UTC),
                weatherPager = WeatherPagerUiModel(
                    pages = listOf(
                        WeatherPageUiModel(
                            placeName = "Berlin",
                            temperature = 10,
                            feltTemperature = "8",
                            windSpeed = "10",
                            chanceOfPrecipitation = "34",
                            humidityPercentage = "48",
                            weatherDescription = "Rainy",
                            weatherIcon = null,
                            backgroundColor = MaterialTheme.colorScheme.primary
                        ),
                        WeatherPageUiModel(
                            placeName = "London",
                            temperature = 10,
                            feltTemperature = "8",
                            windSpeed = "10",
                            chanceOfPrecipitation = "34",
                            humidityPercentage = "48",
                            weatherDescription = "Rainy",
                            weatherIcon = null,
                            backgroundColor = MaterialTheme.colorScheme.secondary
                        ),
                    )
                )
            ),
            onOpenPlaceSelection = { }
        )
    }
}


@Preview
@Composable
private fun WeatherDetailCardPreview() {
    SmooothWeatherTheme {
        WeatherDetailCard(
            feltTemperature = "3°",
            windSpeed = "20 km/h",
            chanceOfPrecipitation = "23 %",
            humidityPercentage = "30%",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

