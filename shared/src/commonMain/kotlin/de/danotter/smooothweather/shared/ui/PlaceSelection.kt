package de.danotter.smooothweather.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import de.danotter.smooothweather.shared.ui.place.PlaceItemUiModel
import de.danotter.smooothweather.shared.ui.place.PlaceSelectionErrorUiModel
import de.danotter.smooothweather.shared.ui.place.PlaceSelectionItemsUiModel
import de.danotter.smooothweather.shared.ui.place.PlaceSelectionUiModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaceSelectionDialog(
    dialogState: DialogState,
    uiModel: PlaceSelectionUiModel,
    onQueryChange: (query: String) -> Unit,
    onSelectPlace: (id: String) -> Unit,
) {
    Dialog(
        dialogState = dialogState,
        onDismiss = {
            onQueryChange("")
            dialogState.hide()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            PlaceSelection(
                uiModel = uiModel,
                onClose = {
                    onQueryChange("")
                    dialogState.hide()
                },
                onQueryChange = onQueryChange,
                onSelectPlace = {
                    dialogState.hide()
                    onSelectPlace(it)
                }
            )
        }
    }
}

@Composable
fun PlaceSelection(
    uiModel: PlaceSelectionUiModel,
    onSelectPlace: (placeId: String) -> Unit,
    onClose: () -> Unit = {},
    onQueryChange: (query: String) -> Unit = {},
) {
    var searchText by rememberSaveable {
        mutableStateOf("")
    }

    LaunchedEffect(searchText) {
        onQueryChange(searchText)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val searchFocusRequester = remember { FocusRequester() }
        SearchBar(
            searchText,
            onClose = onClose,
            onTextChange = { text ->
                searchText = text
            },
            focusRequester = searchFocusRequester
        )

        LaunchedEffect(Unit) {
            searchFocusRequester.requestFocus()
        }

        Divider()

        when (uiModel) {
            is PlaceSelectionErrorUiModel -> {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = "There was an error loading place data.")
                }
            }
            is PlaceSelectionItemsUiModel -> {
                LazyColumn {
                    items(uiModel.items) { item ->
                        PlaceItem(
                            item,
                            onClick = {
                                onSelectPlace(item.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    searchText: String,
    onClose: () -> Unit,
    onTextChange: (value: String) -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close"
                // TODO
                //stringResource(id = R.string.close)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            val textStyle: TextStyle = LocalTextStyle.current

            if (searchText.isEmpty()) {
                Text("Search locations", color = textStyle.color.copy(0.5f))
            }

            BasicTextField(
                value = searchText,
                textStyle = textStyle,
                onValueChange = onTextChange,
                modifier = Modifier
                    .focusRequester(focusRequester)
            )
        }
    }
}

@Composable
private fun PlaceItem(
    item: PlaceItemUiModel,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(all = 16.dp)
    ) {
        Text(
            text = item.text
        )
    }
}
