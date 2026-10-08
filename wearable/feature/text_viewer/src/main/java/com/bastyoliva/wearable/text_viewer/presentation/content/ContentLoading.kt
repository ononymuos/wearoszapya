package com.bastyoliva.wearable.text_viewer.presentation.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices

@Composable
fun ContentLoading(){
    Box(
        modifier = Modifier
            .fillMaxSize()
    ){
        CircularProgressIndicator(
            modifier = Modifier
                .align(alignment = Alignment.Center)
        )
    }
}

@Composable
@WearPreviewDevices
fun ContentLoadingPreview(){
    ContentLoading()
}