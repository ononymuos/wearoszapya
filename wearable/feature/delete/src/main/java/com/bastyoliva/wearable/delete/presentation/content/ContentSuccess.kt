package com.bastyoliva.wearable.delete.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.bastyoliva.wearable.delete.R
import com.bastyoliva.wearable.delete.presentation.Event
import com.bastyoliva.wearable.design.components.basic_screens.DialogDefaultScreen
import com.bastyoliva.wearable.design.icons.Icons
import com.bastyoliva.wearable.design.theme.WearOsZapyaTheme

@Composable
fun ContentSuccess(
    paths: List<String>,
    onEvent: (Event) -> Unit,
) {

    DialogDefaultScreen(
        title = stringResource(R.string.delete_title),
        onCancel = {
            onEvent(Event.OnNavigateBack)
        },
        onOk = {
            onEvent(Event.OnDelete(paths))
        },
        okIcon = Icons.Delete,
        cancelIcon = Icons.ArrowBack
    )

}

@Composable
@Preview(device = "id:wearos_small_round", showSystemUi = false, showBackground = true)
fun ContentFailedPreview(){
    WearOsZapyaTheme() {
        ContentSuccess(
            emptyList(),{}
        )
    }
}