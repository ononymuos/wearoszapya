package com.bastyoliva.wearable.onboarding

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bastyoliva.wearable.onboarding.presentation.OnBoardingScreen
import com.bastyoliva.wearable.onboarding.presentation.OnBoardingViewModel

@Composable
fun OnBoardingRoute(
    viewModel: OnBoardingViewModel = hiltViewModel()
) {

    val uiState = viewModel.state

    OnBoardingScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        dialogState = viewModel.dialogState
    )

}
