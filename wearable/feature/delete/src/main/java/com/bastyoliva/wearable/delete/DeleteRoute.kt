package com.bastyoliva.wearable.delete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bastyoliva.wearable.delete.presentation.DeleteScreen
import com.bastyoliva.wearable.delete.presentation.DeleteViewModel

@Composable
fun DeleteRoute(
    paths: List<String>,
    viewModel: DeleteViewModel = hiltViewModel()
) {

    val uiState = viewModel.state

    LaunchedEffect(Unit) {
        viewModel.setPaths(paths)
    }


    DeleteScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )

}
