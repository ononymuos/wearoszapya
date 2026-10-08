package com.bastyoliva.wearable.images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bastyoliva.wearable.images.presentation.Event
import com.bastyoliva.wearable.images.presentation.GalleryViewModel
import com.bastyoliva.wearable.images.presentation.GalleyScreen

@Composable
fun GalleryRoute(
    viewModel: GalleryViewModel = hiltViewModel(),
) {

    val uiState = viewModel.state
    val dialogState = viewModel.permissionDialogState

    LaunchedEffect(Unit) {
        viewModel.onEvent(Event.OnLoad)
    }

    GalleyScreen(
        uiState = uiState,
        dialogState = dialogState,
        onEvent = { event ->
            viewModel.onEvent(event)
        },
    )

}
