package de.danotter.smooothweather.shared.domain

expect class GetPlaceTypeaheadUseCase {
    suspend operator fun invoke(query: String): PlaceTypeaheadResult
}

sealed interface PlaceTypeaheadResult

class PlaceTypeheadSuccessResult(
    val items: List<PlaceTypeaheadItem>
) : PlaceTypeaheadResult

class PlaceTypeaheadErrorResult(val error: Throwable?): PlaceTypeaheadResult

class PlaceTypeaheadItem(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)
