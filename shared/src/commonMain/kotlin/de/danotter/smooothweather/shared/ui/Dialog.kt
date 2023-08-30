package de.danotter.smooothweather.shared.ui

import androidx.compose.runtime.Composable

expect class DialogState() {
    fun show()

    fun hide()
}

@Composable
expect fun Dialog(
    dialogState: DialogState,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
)