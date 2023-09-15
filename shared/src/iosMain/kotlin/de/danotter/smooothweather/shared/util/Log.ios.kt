package de.danotter.smooothweather.shared.util

import platform.Foundation.NSLog

actual fun logError(msg: String, throwable: Throwable?) {
    log("ERROR", msg, throwable)
}

actual fun logInfo(msg: String, throwable: Throwable?) {
    log("INFO", msg, throwable)
}

private inline fun log(level: String, msg: String, throwable: Throwable?) {
    NSLog("$level: msg")
}
