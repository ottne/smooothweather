package de.danotter.smooothweather.shared.api

//import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSLog
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataTaskWithURL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// todo will be in kotlin 1.9.0
@RequiresOptIn
private annotation class BetaInteropApi

actual interface OpenMeteoApi {
    actual suspend fun getWeather(
        longitude: Double,
        latitude: Double,
        hourlyValues: String
    ): MeteoWeatherResponse

    companion object {
        fun create(
            baseUrl: String,
            urlSession: NSURLSession,
            json: Json
        ): OpenMeteoApi = OpenMeteoApiImpl(
            baseUrl,
            urlSession,
            json
        )
    }
}

@OptIn(BetaInteropApi::class)
internal class OpenMeteoApiImpl(
    private val baseUrl: String,
    private val urlSession: NSURLSession,
    private val json: Json
) : OpenMeteoApi {

    override suspend fun getWeather(
        longitude: Double,
        latitude: Double,
        hourlyValues: String
    ): MeteoWeatherResponse = urlSession
        .fetchJson<MeteoWeatherResponse>("$baseUrl/v1/forecast?longitude=${longitude}&latitude=${latitude}&hourly=${hourlyValues}")
        .onFailure {  error ->
            NSLog("Error getting response:\n${error}")
        }
        .getOrThrow()

    private suspend inline fun <reified T> NSURLSession.fetchJson(urlString: String): Result<T> = runCatching {
        suspendCancellableCoroutine { cont ->
            val url = NSURL.URLWithString(urlString)!!
            val dataTask = urlSession.dataTaskWithURL(url) { data, response, error ->
                val httpResponse = response as NSHTTPURLResponse

                if (data == null || error != null || httpResponse.statusCode >= 400) {
                    cont.resumeWithException(IllegalStateException("Request failed."))
                    return@dataTaskWithURL
                }

                val string = NSString.create(data, NSUTF8StringEncoding) as String?
                try {
                    val decodedJson: T =
                        json.decodeFromString(string!!)
                    cont.resume(decodedJson)
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }

            dataTask.resume()

            cont.invokeOnCancellation {
                dataTask.cancel()
            }
        }
    }
}
