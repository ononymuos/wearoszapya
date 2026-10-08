package com.bastyoliva.wearable.new_directory.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bastyoliva.wearable.design.components.basic_screens.ContentFailedDefaultScreen
import com.bastyoliva.wearable.new_directory.R
import com.bastyoliva.wearable.new_directory.presentation.Event

@Composable
fun ContentFailed(
    onEvent: (Event) -> Unit,
){
    ContentFailedDefaultScreen(
        title = stringResource(id = R.string.new_dir_failed),
        onBackClick = {onEvent(Event.OnNavigateBack)}
    )
}