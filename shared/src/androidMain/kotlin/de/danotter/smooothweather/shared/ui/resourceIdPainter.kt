package de.danotter.smooothweather.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

@Composable
actual fun resourceIdPainter(resourceId: Int): Painter {
    return painterResource(id = resourceId)
}