package com.bastyoliva.wearable.delete.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bastyoliva.wearable.delete.R
import com.bastyoliva.wearable.delete.presentation.Event
import com.bastyoliva.wearable.design.components.basic_screens.ContentFailedDefaultScreen

@Composable
fun ContentFailed(
    onEvent: (Event) -> Unit,
){
    ContentFailedDefaultScreen(
        title = stringResource(id = R.string.delete_failed),
        onBackClick = {onEvent(Event.OnNavigateBack)}
    )
}