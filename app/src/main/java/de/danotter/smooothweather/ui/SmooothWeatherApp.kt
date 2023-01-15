@file:OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)

package de.danotter.smooothweather.ui

import android.text.format.DateFormat
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.core.os.LocaleListCompat
import de.danotter.smooothweather.*
import de.danotter.smooothweather.ui.pagerindicator.HorizontalPagerIndicator
import de.danotter.smooothweather.ui.theme.SmooothWeatherTheme
import kotlinx.datetime.*
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs
import kotlinx.datetime.TimeZone as KotlinxTimeZone

@Composable
fun SmooothWeatherApp(
    weatherUiModel: WeatherUiModel,
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

    WeatherScreen(
        weatherUiModel,
        onOpenPlaceSelection = {
            placeSelectionTransitionState.targetState = true
        }
    )
}


@Composable
fun WeatherScreen(
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
                        "ddMMMMHm"
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
    val locale = LocaleListCompat.getAdjustedDefault()[0]
    val hourlyTimeFormatter = remember(locale) {
        val pattern = DateFormat.getBestDateTimePattern(locale, "Hm")
        DateTimeFormatter.ofPattern(pattern)
    }
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
                        hourlyTimeFormatter.format(time.toJavaLocalTime())
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
    timeFormatter: (time: LocalTime) -> String,
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
            painterResource(id = iconSpec.resourceId)
        }
    }
}

@Suppress("NAME_SHADOWING")
@Composable
private fun MainTitle(
    title: String,
    subtitle: String,
) {
    DefaultAnimatedContent(
        targetState = title to subtitle
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
            (slideInVertically(animationSpec = intSpring) { height -> height } + fadeIn(spring) with
                    slideOutVertically(animationSpec = intSpring) { height -> -height } + fadeOut(spring))
                .using(
                    SizeTransform(clip = false)
                )
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

@Composable
@Preview
fun MainScreenPreview() {
    SmooothWeatherTheme {
        WeatherScreen(
            uiModel = WeatherSuccessUiModel(
                currentDateTime = LocalDateTime(
                    year = 2022,
                    monthNumber = 1,
                    dayOfMonth = 8,
                    hour = 12,
                    minute = 34,
                ).toInstant(KotlinxTimeZone.UTC),
                weatherPager = WeatherPagerUiModel(
                    pages = listOf(
                        WeatherPageUiModel(
                            placeName = "Berlin",
                            temperature = 10,
                            feltTemperature = 8,
                            windSpeed = 10,
                            chanceOfPrecipitation = "34",
                            humidityPercentage = "48",
                            weatherDescription = "Rainy",
                            weatherIcon = null,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            hourlyWeather = emptyList()
                        ),
                        WeatherPageUiModel(
                            placeName = "London",
                            temperature = 10,
                            feltTemperature = 8,
                            windSpeed = 10,
                            chanceOfPrecipitation = "34",
                            humidityPercentage = "48",
                            weatherDescription = "Rainy",
                            weatherIcon = null,
                            backgroundColor = MaterialTheme.colorScheme.secondary,
                            hourlyWeather = emptyList()
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

@Preview
@Composable
private fun AnimatedNumberPlayground() {
    var textFieldValue by remember { mutableStateOf("") }
    var number by remember { mutableStateOf(0) }

    val animatedNumber by animateTemperatureAsState(number)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            "Value: $animatedNumber",

        )

        TextField(
            textFieldValue,
            onValueChange = { value: String ->
                textFieldValue = value
            }
        )

        Button(
            onClick = {
                textFieldValue.toIntOrNull()?.let { number = it }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Set number")
        }
    }
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
