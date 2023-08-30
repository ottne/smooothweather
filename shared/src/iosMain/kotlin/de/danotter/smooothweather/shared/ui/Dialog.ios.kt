package de.danotter.smooothweather.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIPresentationController
import platform.UIKit.UIViewController
import platform.UIKit.presentationController
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Stable
actual class DialogState {
    internal val isShowDialog: MutableState<Boolean> = mutableStateOf(false)

    actual fun show() {
        isShowDialog.value = true
    }

    actual fun hide() {
        isShowDialog.value = false
    }
}

@Composable
actual fun Dialog(
    dialogState: DialogState,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val viewController = LocalUIViewController.current

    var currentPresentedViewController by remember {
        mutableStateOf<UIViewController?>(null, policy = referentialEqualityPolicy())
    }

    val presentationControllerDelegate =
        object : NSObject(), UIAdaptivePresentationControllerDelegateProtocol {
            override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
                currentPresentedViewController = null
                dialogState.isShowDialog.value = false
            }
        }

    LaunchedEffect(dialogState.isShowDialog.value) {
        if (dialogState.isShowDialog.value && viewController.presentedViewController == null) {
            currentPresentedViewController = ComposeUIViewController { content() }.apply {
                presentationController?.delegate = presentationControllerDelegate
            }

            viewController.presentViewController(
                viewControllerToPresent = currentPresentedViewController!!
            )
        } else if (viewController.presentedViewController == currentPresentedViewController) {
            currentPresentedViewController = null
            viewController.dismissViewController()
        }
    }
}

suspend fun UIViewController.presentViewController(
    viewControllerToPresent: UIViewController,
    animated: Boolean = true
) {
    suspendCoroutine { cont ->
        presentViewController(viewControllerToPresent, animated = animated, completion = {
            cont.resume(Unit)
        })
    }
}

suspend fun UIViewController.dismissViewController(
    animated: Boolean = true
) {
    suspendCoroutine { cont ->
        dismissViewControllerAnimated(flag = animated, completion = {
            cont.resume(Unit)
        })
    }
}
