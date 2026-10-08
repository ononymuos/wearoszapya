package com.bastyoliva.wearable.onboarding.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.bastyoliva.wearable.design.icons.Icons
import com.bastyoliva.wearable.onboarding.R
import com.bastyoliva.wearable.onboarding.presentation.content.ContentFailed
import com.bastyoliva.wearable.onboarding.presentation.content.ContentLoading
import com.bastyoliva.wearable.onboarding.presentation.content.ContentSuccess
import com.bastyoliva.wearable.onboarding.presentation.content.DialogState
import com.bastyoliva.wearable.onboarding.presentation.content.UiState

@Composable
fun OnBoardingScreen(
    onEvent: (Event) -> Unit,
    uiState: UiState,
    dialogState: DialogState = DialogState.CLOSED
) {

    Dialog(
        showDialog = dialogState != DialogState.CLOSED,
        onDismissRequest = {
            onEvent(Event.CloseDialog)
        }
    )
    {
        Alert(
            icon = {
                if (dialogState == DialogState.SUCCESS){
                    Icon(
                        imageVector = Icons.Check,
                        contentDescription = stringResource(R.string.success)
                    )
                } else if (dialogState == DialogState.FAILED){
                    Icon(
                        imageVector = Icons.Error,
                        contentDescription = stringResource(R.string.failed)
                    )
                }

            },
            title = {

                Text(
                    text = when (dialogState) {
                        DialogState.SUCCESS -> {
                            stringResource(R.string.send_success)
                        }
                        DialogState.FAILED -> {
                            stringResource(R.string.send_failed)
                        }
                        else -> ""
                    },
                    textAlign = TextAlign.Center
                )
            },

            ){

        }
    }

    when(uiState){
        UiState.LOADING -> ContentLoading()
        UiState.SUCCESS -> ContentSuccess()
        UiState.FAILED -> ContentFailed(onEvent = onEvent)
    }

}

@Composable
@Preview(device = "id:wearos_small_round", showBackground = true, showSystemUi = false)
fun OnBoardingScreenPreview(){
    OnBoardingScreen({}, UiState.LOADING)
}


