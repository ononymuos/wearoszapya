package com.bastyoliva.wearable.text_viewer.presentation.content

import androidx.compose.runtime.Composable
import com.bastyoliva.wearable.design.components.basic_screens.ContentFailedDefaultScreen

@Composable
fun ContentFailed() {
    ContentFailedDefaultScreen(
        title = "Error",
        onBackClick = {}
    )
}