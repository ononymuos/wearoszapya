package com.bastyoliva.wearable.file_list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bastyoliva.wearable.file_list.presentation.Event
import com.bastyoliva.wearable.file_list.presentation.FileListScreen
import com.bastyoliva.wearable.file_list.presentation.FileListViewModel

@Composable
fun FileListRoute(
    viewModel: FileListViewModel = hiltViewModel(),
    path: String? = null
) {

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val menuState by viewModel.menuState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, path) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.onEvent(Event.OnGetFileListAtPath(path))
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    FileListScreen(
        uiState = uiState,
        menuState = menuState,
        onEvent = { event ->
            viewModel.onEvent(event)
        },
    )

}
