package com.bastyoliva.wearable.menu.presentation

import androidx.compose.runtime.Composable
import com.bastyoliva.wearable.menu.presentation.content.ContentLoading
import com.bastyoliva.wearable.menu.presentation.content.ContentSuccess
import com.bastyoliva.wearable.menu.presentation.content.OperationWorking
import com.bastyoliva.wearable.menu.presentation.content.UiState

@Composable
fun MenuScreen(onEvent: (Event) -> Unit, uiState: UiState) {

    when(uiState){
        is UiState.Loading -> ContentLoading()
        is UiState.Success -> ContentSuccess(
            name = uiState.name,
            actions = uiState.actions,
            onEvent = onEvent
        )
        is UiState.Failed -> {
            onEvent(Event.OnNavigateBack)
        }
        is UiState.Operation -> {
            OperationWorking()
        }
     }

}
