package com.bastyoliva.wearable.rename.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.bastyoliva.wearable.design.components.basic_screens.TextFieldDefaultScreen
import com.bastyoliva.wearable.design.theme.WearOsZapyaTheme
import com.bastyoliva.wearable.rename.R
import com.bastyoliva.wearable.rename.presentation.Event

@Composable
fun ContentSuccess(
    path: String,
    newFileName: String,
    isSaveEnabled: Boolean,
    onEvent: (Event) -> Unit,
) {

    TextFieldDefaultScreen(
        isSaveEnabled = isSaveEnabled,
        onSaveClick = {
            onEvent(
                Event.OnRename(
                    path = path,
                    newName = newFileName
                )
            )
        },
        onValueChange = { onEvent(Event.OnNewFileNameChanged(it)) },
        value = newFileName,
        label = stringResource(R.string.rename_name)
    )

}

@Composable
@Preview(device = "id:wearos_small_round", showSystemUi = false, showBackground = true)
fun ContentFailedPreview(){
    WearOsZapyaTheme() {
        ContentSuccess(
            "33333","22222",  true, {}
        )
    }
}