package com.bastyoliva.wearable.text_viewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bastyoliva.wearable.text_viewer.presentation.Event
import com.bastyoliva.wearable.text_viewer.presentation.TextViewerScreen
import com.bastyoliva.wearable.text_viewer.presentation.TextViewerViewModel

@Composable
fun TextViewerRoute(
    uriString: String,
    viewModel: TextViewerViewModel = hiltViewModel(),
) {

    val uiState = viewModel.state

    LaunchedEffect(Unit) {
        viewModel.onEvent(Event.OnGetFileTextContent(uriString))
    }

    TextViewerScreen(uiState = uiState)

}