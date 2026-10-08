package com.bastyoliva.wearable.theming.presentation

import androidx.compose.runtime.Composable
import com.bastyoliva.wearable.design.components.basic_screens.ContentLoadingDefaultScreen

@Composable
fun ThemingScreen(onEvent: (Event) -> Unit, uiState: UiState) {

    when(uiState){
        is UiState.Loading -> ContentLoadingDefaultScreen()
        is UiState.Success -> ContentSuccess(
            uiState.colors,
            uiState.selectedColor,
            onEvent
            )
        is UiState.Failed -> {
            onEvent(Event.OnNavigateBack)
        }
     }

}
