package com.bastyoliva.wearable.theming

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bastyoliva.wearable.theming.presentation.ThemingScreen
import com.bastyoliva.wearable.theming.presentation.ThemingViewModel

@Composable
fun ThemingRoute(
    viewModel: ThemingViewModel = hiltViewModel()
) {

    val uiState = viewModel.state

    ThemingScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )

}
