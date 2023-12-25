package de.danotter.smooothweather.shared.ui.place

import de.danotter.smooothweather.shared.domain.AddPlaceUseCase
import de.danotter.smooothweather.shared.domain.GetPlaceTypeaheadUseCase
import de.danotter.smooothweather.shared.domain.Place
import de.danotter.smooothweather.shared.domain.PlaceTypeaheadErrorResult
import de.danotter.smooothweather.shared.domain.PlaceTypeaheadItem
import de.danotter.smooothweather.shared.domain.PlaceTypeheadSuccessResult
import de.danotter.smooothweather.shared.ui.BaseViewModel
import de.danotter.smooothweather.shared.util.logError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaceSelectionViewModel(
    getPlaceTypeahead: GetPlaceTypeaheadUseCase,
    private val addPlace: AddPlaceUseCase
) : BaseViewModel() {

    private val query = MutableStateFlow("")

    private val latestTypeaheads = MutableStateFlow<List<PlaceTypeaheadItem>>(emptyList())

    val uiModel: StateFlow<PlaceSelectionUiModel> = query
        .debounce(timeoutMillis = 350)
        .map(String::trim)
        .distinctUntilChanged()
        .mapLatest { query ->
            if (query.isBlank()) {
                // shortcut if no characters entered
                return@mapLatest PlaceSelectionItemsUiModel(
                    items = emptyList()
                )
            }
            when (val typeaheads = getPlaceTypeahead(query)) {
                is PlaceTypeheadSuccessResult -> {
                    latestTypeaheads.update { typeaheads.items }

                    PlaceSelectionItemsUiModel(
                        items = typeaheads.items.map { typeaheadItem ->
                            PlaceItemUiModel(
                                id = typeaheadItem.id,
                                text = typeaheadItem.name
                            )
                        }
                    )
                }

                is PlaceTypeaheadErrorResult -> {
                    logError("Error getting places results", typeaheads.error)
                    PlaceSelectionErrorUiModel(error = typeaheads.error)
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PlaceSelectionItemsUiModel(items = emptyList())
        )

    fun setQuery(query: String) {
        this.query.update { query }
    }

    fun selectPlace(id: String) {
        viewModelScope.launch {
            val typeaheadItem = latestTypeaheads.value.find { it.id == id }

            if (typeaheadItem != null) {
                addPlace(
                    Place(
                        placeName = typeaheadItem.name,
                        latitude = typeaheadItem.latitude,
                        longitude = typeaheadItem.longitude
                    )
                )
            }
        }
    }
}

sealed interface PlaceSelectionUiModel

data class PlaceSelectionItemsUiModel(
    val items: List<PlaceItemUiModel>
) : PlaceSelectionUiModel

class PlaceSelectionErrorUiModel(
    error: Throwable?
) : PlaceSelectionUiModel

data class PlaceItemUiModel(
    val id: String,
    val text: String
)
