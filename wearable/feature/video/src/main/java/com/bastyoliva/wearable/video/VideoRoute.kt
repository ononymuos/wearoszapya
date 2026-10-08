package com.bastyoliva.wearable.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bastyoliva.wearable.video.presentation.Event
import com.bastyoliva.wearable.video.presentation.VideoViewModel
import com.bastyoliva.wearable.video.presentation.GalleyScreen

@Composable
fun VideoRoute(
    viewModel: VideoViewModel = hiltViewModel(),
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
