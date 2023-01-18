SmooothWeather Android App
==================

Simple weather app that lets you view weather information around the world. It is written using the
following technologies:

* UI: Jetpack Compose + Material 3
* Persistence: SQLDelight
* Networking: Retrofit / OkHttp
* Dependency injection: Dagger Hilt

The data is sourced from [https://open-meteo.com/](Open-Meteo)'s API. For location search, the 
standard platform's Geocoder API is used.

### Building and running

Run the app using the usual `debug` or `release` configurations, either from Android Studio or the
command line:

```
> ./gradlew :app:assembleRelease
> adb install app/build/outputs/apk/release/app-release.apk 
```
