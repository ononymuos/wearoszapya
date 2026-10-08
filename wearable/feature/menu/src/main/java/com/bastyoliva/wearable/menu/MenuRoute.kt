package com.bastyoliva.wearable.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bastyoliva.wearable.menu.presentation.Event
import com.bastyoliva.wearable.menu.presentation.MenuScreen
import com.bastyoliva.wearable.menu.presentation.MenuViewModel

@Composable
fun MenuRoute(
    viewModel: MenuViewModel = hiltViewModel(),
    paths: List<String>,
    onDismissRequest: () -> Unit = {},
    mode: MenuMode = MenuMode.OUTSIDE
) {

    val uiState = viewModel.state

    LaunchedEffect(paths) {
        viewModel.onEvent(Event.OnGetMenuActions(paths, mode))
        viewModel.setOnDismiss(onDismissRequest)
    }

    MenuScreen(
        uiState = uiState, onEvent = { event ->
            viewModel.onEvent(event)
        })

}

enum class MenuMode {
    INSIDE, OUTSIDE, PINNED
}
