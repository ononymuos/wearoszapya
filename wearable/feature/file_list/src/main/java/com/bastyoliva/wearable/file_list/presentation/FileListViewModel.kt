package com.bastyoliva.wearable.file_list.presentation

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastyoliva.wearable.data.model.PrettyPath
import com.bastyoliva.wearable.file_list.presentation.content.MenuState
import com.bastyoliva.wearable.file_list.presentation.content.UiState
import com.bastyoliva.wearable.file_list.usecase.CheckFileAccessUseCase
import com.bastyoliva.wearable.file_list.usecase.GetActionsUseCase
import com.bastyoliva.wearable.file_list.usecase.GetBasePathUseCase
import com.bastyoliva.wearable.file_list.usecase.GetFileListUseCase
import com.bastyoliva.wearable.file_list.usecase.GetParentUseCase
import com.bastyoliva.wearable.file_list.usecase.NavigateBackUseCase
import com.bastyoliva.wearable.file_list.usecase.NavigateToPathUseCase
import com.bastyoliva.wearable.file_list.usecase.OpenFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileListViewModel @Inject constructor(
    private val navigateToPathUseCase: NavigateToPathUseCase,
    private val getFileListUseCase: GetFileListUseCase,
    private val getBasePathUseCase: GetBasePathUseCase,
    private val getParentUseCase: GetParentUseCase,
    private val openFileUseCase: OpenFileUseCase,
    private val navigateBackUseCase: NavigateBackUseCase,
    private val getActionsUseCase: GetActionsUseCase,
    private val checkFileAccessUseCase: CheckFileAccessUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _menuState = MutableStateFlow<MenuState>(MenuState.Hide)
    val menuState: StateFlow<MenuState> = _menuState.asStateFlow()

    private var fileListJob: Job? = null

    fun onEvent(event: Event) {
        when (event) {
            is Event.OnGetFileListAtPath -> {

                val hasFileAccess = checkFileAccessUseCase()
                val path = event.path ?: getBasePathUseCase()
                val isReceivedFolder = path.contains("Download/Received", ignoreCase = true)

                if (!hasFileAccess && !isReceivedFolder) {
                    _state.value = UiState.NoPermissions
                } else {
                    getFileListAtPath(path)
                }
            }

            is Event.OnFileClick -> {
                openFileUseCase(event.file)
            }

            is Event.OnDirectoryClick -> {
                navigateToPathUseCase(event.path)
            }

            is Event.OnNavigateBack -> {
                navigateBackUseCase()
            }

            is Event.OnNavigateToParent -> {
                navigateToParent(event.path)
            }

            is Event.OnHideMenu -> {
                _menuState.value = MenuState.Hide
            }

            is Event.OnShowMenuFor -> {
                _menuState.value = MenuState.Show(event.paths, event.menuMode)
            }

        }
    }


    private fun navigateToParent(path: String) {
        viewModelScope.launch {
            val parentPath = getParentUseCase(path)
            if (parentPath != null) {
                navigateToPathUseCase(parentPath)
            }
        }
    }

    private fun getFileListAtPath(path: String) {
        fileListJob?.cancel()
        fileListJob = viewModelScope.launch {
            val currentState = _state.value
            if (currentState is UiState.Success && currentState.path.path == path) {
                // Keep the current state to avoid UI flicker and scroll reset
            } else {
                _state.value = UiState.Loading
            }

            getFileListUseCase(path).collect { result ->
                result.onSuccess { fileList ->

                    val actions = getActionsUseCase(path)

                    var homePath = getBasePathUseCase()

                    val downloadsPath =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                    val receivedPath = File(downloadsPath, "Received").absolutePath

                    if (path.startsWith(receivedPath)) {
                        homePath = receivedPath
                    }

                    _state.value = UiState.Success(
                        path = PrettyPath(
                            homePath, path
                        ), files = fileList, actions = actions
                    )

                }.onFailure { e ->
                    _state.value = UiState.Failed(e)
                }
            }
        }
    }


}
