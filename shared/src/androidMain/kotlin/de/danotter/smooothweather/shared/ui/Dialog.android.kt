package de.danotter.smooothweather.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.window.DialogProperties

actual class DialogState {
    internal val transitionState = MutableTransitionState(initialState = false)

    actual fun show() {
        transitionState.targetState = true
    }

    actual fun hide() {
        transitionState.targetState = false
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Dialog(
    dialogState: DialogState,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {

    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    val transitionState = dialogState.transitionState

    LaunchedEffect(transitionState.targetState) {
        if (!transitionState.targetState) {
            softwareKeyboardController?.hide()
        }
    }

    if (!transitionState.isIdle || transitionState.currentState) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
        ) {
            AnimatedVisibility(
                visibleState = transitionState,
                enter = slideInVertically(
                    initialOffsetY = { it },
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                ),
                content = {
                    content()
                }
            )
        }
    }
}