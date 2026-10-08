package com.bastyoliva.wearable.delete.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bastyoliva.wearable.delete.presentation.content.ContentFailed
import com.bastyoliva.wearable.delete.presentation.content.ContentSuccess


@Composable
fun DeleteScreen(
    uiState: UiState,
    onEvent: (Event) -> Unit,
) {


    when(uiState){
        is UiState.Failed -> {
            ContentFailed(onEvent)
        }
        is UiState.Loading -> {
        }
        is UiState.Success -> {
            ContentSuccess(
                paths = uiState.paths,
                onEvent = onEvent,
            )
        }

    }

}

@Composable
@Preview(device = "id:wearos_small_round", showBackground = true, showSystemUi = false)
fun DeleteScreenPreview(){
    DeleteScreen(uiState = UiState.Loading, {})
}