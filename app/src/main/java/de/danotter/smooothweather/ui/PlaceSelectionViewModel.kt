package de.danotter.smooothweather.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.danotter.smooothweather.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlaceSelectionViewModel @Inject constructor(
    getPlaceTypeahead: GetPlaceTypeaheadUseCase,
    private val addPlace: AddPlaceUseCase
) : ViewModel() {

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
                    Timber.e(typeaheads.error, "Error getting places results")
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
                addPlace(Place(
                    placeName = typeaheadItem.name,
                    latitude = typeaheadItem.latitude,
                    longitude = typeaheadItem.longitude
                ))
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
