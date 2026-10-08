package com.bastyoliva.wearable.new_directory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bastyoliva.wearable.new_directory.presentation.NewDirectoryScreen
import com.bastyoliva.wearable.new_directory.presentation.NewDirectoryViewModel

@Composable
fun NewDirectoryRoute(
    path: String,
    viewModel: NewDirectoryViewModel = hiltViewModel()
) {

    val uiState = viewModel.state

    LaunchedEffect(Unit) {
        viewModel.setPath(path)
    }


    NewDirectoryScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )

}
