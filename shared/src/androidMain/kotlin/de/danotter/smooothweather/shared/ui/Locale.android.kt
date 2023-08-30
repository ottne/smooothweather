package de.danotter.smooothweather.shared.ui

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.os.LocaleListCompat
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalTime
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date

@Composable
actual fun formatHourlyTime(time: LocalTime): String {
    val locale = LocaleListCompat.getAdjustedDefault()[0]
    val hourlyTimeFormatter = remember(locale) {
        val pattern = DateFormat.getBestDateTimePattern(locale, "Hm")
        DateTimeFormatter.ofPattern(pattern)
    }

    return hourlyTimeFormatter.format(time.toJavaLocalTime())
}

@Composable
actual fun formatDateTime(instant: Instant): String {
    val locale = LocaleListCompat.getAdjustedDefault()[0]
    return remember(locale) {
        val pattern = DateFormat.getBestDateTimePattern(
            locale,
            "ddMMMMHm"
        )
        val format = SimpleDateFormat(pattern, locale)
        val date = Date.from(instant.toJavaInstant())
        format.format(date)
    }
}