package com.bastyoliva.wearable.menu.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastyoliva.wearable.menu.presentation.content.UiState
import com.bastyoliva.wearable.menu.usecase.CopyUseCase
import com.bastyoliva.wearable.menu.usecase.CutUseCase
import com.bastyoliva.wearable.menu.usecase.GetMenuActionsUseCase
import com.bastyoliva.wearable.menu.usecase.IsDirectoryUseCase
import com.bastyoliva.wearable.menu.usecase.NavigateToDeleteUseCase
import com.bastyoliva.wearable.menu.usecase.NavigateToNewDirectoryUseCase
import com.bastyoliva.wearable.menu.usecase.NavigateToPathUseCase
import com.bastyoliva.wearable.menu.usecase.NavigateToRenameUseCase
import com.bastyoliva.wearable.menu.usecase.OpenFileUseCase
import com.bastyoliva.wearable.menu.usecase.PasteCancelUseCase
import com.bastyoliva.wearable.menu.usecase.PasteHereUseCase
import com.bastyoliva.wearable.menu.usecase.PinUseCase
import com.bastyoliva.wearable.menu.usecase.UnpinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val getMenuActionsUseCase: GetMenuActionsUseCase,
    private val navigateToRenameUseCase: NavigateToRenameUseCase,
    private val navigateToNewDirectoryUseCase: NavigateToNewDirectoryUseCase,
    private val navigateToPathUseCase: NavigateToPathUseCase,
    private val openFileUseCase: OpenFileUseCase,
    private val isDirectoryUseCase: IsDirectoryUseCase,
    private val navigateToDeleteUseCase: NavigateToDeleteUseCase,
    private val pinUseCase: PinUseCase,
    private val unpinUseCase: UnpinUseCase,
    private val cutUseCase: CutUseCase,
    private val copyUseCase: CopyUseCase,
    private val pasteHereUseCase: PasteHereUseCase,
    private val cancelPasteUseCase: PasteCancelUseCase

) : ViewModel() {

    var state by mutableStateOf<UiState>(UiState.Loading)
        private set

    private var navigateBackUseCase: () -> Unit = {  }

    fun setOnDismiss(onDismissRequest: () -> Unit) {
        navigateBackUseCase = onDismissRequest
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.OnNavigateBack -> {
                navigateBackUseCase()
            }

            is Event.OnHeaderClick -> {
                viewModelScope.launch {
                    val isDirectory = isDirectoryUseCase(path = event.path)
                    if (isDirectory) {
                        onEvent(
                            Event.OnDirectoryClick(
                                path = event.path
                            )
                        )
                    } else {
                        onEvent(
                            Event.OnFileClick(
                                path = event.path
                            )
                        )
                    }
                    navigateBackUseCase()
                }
            }

            is Event.OnNavigateToRename -> {
                navigateToRenameUseCase(event.path)
                navigateBackUseCase()
            }

            is Event.OnNavigateToDelete -> {
                navigateToDeleteUseCase(event.paths)
                navigateBackUseCase()
            }

            is Event.OnNavigateToNewDirectory -> {
                navigateToNewDirectoryUseCase(event.path)
                navigateBackUseCase()
            }

            is Event.OnDirectoryClick -> {
                navigateToPathUseCase(event.path)
                navigateBackUseCase()
            }

            is Event.OnFileClick -> {
                openFileUseCase(event.path)
                navigateBackUseCase()
            }

            is Event.OnGetMenuActions -> {
                viewModelScope.launch {
                    val mode = event.mode
                    val actions = getMenuActionsUseCase(event.paths, mode)
                    state = UiState.Success(
                        name = if (event.paths.size == 1) event.paths.first().split("/")
                            .last() else "", actions = actions
                    )
                }
            }

            is Event.OnPin -> {
                viewModelScope.launch {
                    pinUseCase(event.paths)
                    navigateBackUseCase()
                }
            }

            is Event.OnUnpin -> {
                viewModelScope.launch {
                    unpinUseCase(event.paths)
                    navigateBackUseCase()
                }
            }

            is Event.OnCancelPaste -> {
                cancelPasteUseCase()
                navigateBackUseCase()
            }

            is Event.OnCopy -> {
                copyUseCase(event.paths)
                navigateBackUseCase()
            }
            is Event.OnCut -> {
                cutUseCase(event.paths)
                navigateBackUseCase()
            }
            is Event.OnPaste -> {
                viewModelScope.launch {
                    state = UiState.Operation
                    pasteHereUseCase(event.path)
                    navigateBackUseCase()
                }

            }
        }
    }


}
