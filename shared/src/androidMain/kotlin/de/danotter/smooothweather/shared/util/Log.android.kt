@file:Suppress("NOTHING_TO_INLINE")

package de.danotter.smooothweather.shared.util

import timber.log.Timber

actual inline fun logError(msg: String, throwable: Throwable?) {
    Timber.e(msg, throwable)
}

actual inline fun logInfo(msg: String, throwable: Throwable?) {
    Timber.i(msg, throwable)
}
