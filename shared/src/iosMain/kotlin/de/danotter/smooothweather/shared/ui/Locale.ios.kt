package de.danotter.smooothweather.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDateFormatter

@Composable
actual fun formatHourlyTime(time: LocalTime): String {
    val dateFormatter = remember {
        val format = NSDateFormatter.dateFormatFromTemplate(
            tmplate = "hhmm",
            options = 0U,
            locale = null
        )
        NSDateFormatter().apply {
            setDateFormat(format)
        }
    }

    val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return dateFormatter.stringFromDate(time.atDate(date).toInstant(TimeZone.currentSystemDefault()).toNSDate())
}

@Composable
actual fun formatDateTime(instant: Instant): String {

    val dateFormatter = remember {
        val format = NSDateFormatter.dateFormatFromTemplate(
            tmplate = "hhmmdd",
            options = 0U,
            locale = null
        )
        NSDateFormatter().apply {
            setDateFormat(format)
        }
    }

    return dateFormatter.stringFromDate(instant.toNSDate())
}
