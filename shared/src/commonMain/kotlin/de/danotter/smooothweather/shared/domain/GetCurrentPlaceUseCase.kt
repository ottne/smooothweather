package de.danotter.smooothweather.shared.domain

expect class GetCurrentPlaceUseCase {
    suspend operator fun invoke(): CurrentPlaceResult
}

sealed interface CurrentPlaceResult

data class CurrentPlaceSuccessResult(
    val locality: String,
    val longitude: Double,
    val latitude: Double
) : CurrentPlaceResult

object CurrentPlacePermissionNotGranted : CurrentPlaceResult

class CurrentPlaceFailureResult(val error: Throwable? = null) : CurrentPlaceResult
