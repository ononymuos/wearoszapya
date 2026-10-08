package com.bastyoliva.wearable.rename

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bastyoliva.wearable.rename.presentation.Event
import com.bastyoliva.wearable.rename.presentation.RenameScreen
import com.bastyoliva.wearable.rename.presentation.RenameViewModel

@Composable
fun RenameRoute(
    path: String,
    viewModel: RenameViewModel = hiltViewModel()
) {

    val uiState = viewModel.state

    LaunchedEffect(Unit) {
        viewModel.onEvent(Event.OnGetFileName(path))
    }


    RenameScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )

}
