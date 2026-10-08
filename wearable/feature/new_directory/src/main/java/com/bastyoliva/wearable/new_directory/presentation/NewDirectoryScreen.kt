package com.bastyoliva.wearable.new_directory.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bastyoliva.wearable.new_directory.presentation.content.ContentFailed
import com.bastyoliva.wearable.new_directory.presentation.content.ContentSuccess


@Composable
fun NewDirectoryScreen(
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
                fileName = uiState.fileName,
                onEvent = onEvent,
                isSaveEnabled = uiState.isSaveEnabled()
            )
        }

    }

}

@Composable
@Preview(device = "id:wearos_small_round", showBackground = true, showSystemUi = false)
fun NewDirectoryScreenPreview(){
    NewDirectoryScreen(uiState = UiState.Loading, {})
}