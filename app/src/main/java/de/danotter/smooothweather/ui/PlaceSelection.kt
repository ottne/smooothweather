package de.danotter.smooothweather.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.danotter.smooothweather.R

@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun PlaceSelectionDialog(
    transitionState: MutableTransitionState<Boolean>,
    uiModel: PlaceSelectionUiModel,
    onQueryChange: (query: String) -> Unit,
    onSelectPlace: (id: String) -> Unit,
) {
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(transitionState.targetState) {
        if (!transitionState.targetState) {
            softwareKeyboardController?.hide()
        }
    }

    if (!transitionState.isIdle || transitionState.currentState) {
        Dialog(
            onDismissRequest = {
                onQueryChange("")
                transitionState.targetState = false
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            AnimatedVisibility(
                visibleState = transitionState,
                enter = slideInVertically(
                    initialOffsetY = { it },
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                )
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
                            transitionState.targetState = false
                        },
                        onQueryChange = onQueryChange,
                        onSelectPlace = {
                            transitionState.targetState = false
                            onSelectPlace(it)
                        }
                    )
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
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
                contentDescription = stringResource(id = R.string.close)
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
