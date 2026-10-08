package com.bastyoliva.wearable.rename.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bastyoliva.wearable.rename.presentation.content.ContentFailed
import com.bastyoliva.wearable.rename.presentation.content.ContentSuccess


@Composable
fun RenameScreen(
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
                path = uiState.path,
                newFileName = uiState.newFileName,
                onEvent = onEvent,
                isSaveEnabled = uiState.isSaveEnabled()
            )
        }

    }

}

@Composable
@Preview(device = "id:wearos_small_round", showBackground = true, showSystemUi = false)
fun RenameScreenPreview(){
    RenameScreen(uiState = UiState.Loading, {})
}