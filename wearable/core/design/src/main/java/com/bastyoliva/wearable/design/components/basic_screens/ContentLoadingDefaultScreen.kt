package com.bastyoliva.wearable.design.components.basic_screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ContentLoadingDefaultScreen() {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000.milliseconds)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            modifier = Modifier
                .align(alignment = Alignment.Center)
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
@Preview(device = "id:wearos_xl_round", showBackground = true)
fun ContentLoadingDefaultScreenPreview() {
    ContentLoadingDefaultScreen()
}