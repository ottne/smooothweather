package de.danotter.smooothweather.shared.ui

import androidx.compose.runtime.Composable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime

@Composable
expect fun formatHourlyTime(time: LocalTime): String

@Composable
expect fun formatDateTime(instant: Instant): String
