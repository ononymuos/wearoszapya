package com.bastyoliva.wearable.video.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastyoliva.wearable.video.presentation.content.PermissionDialogState
import com.bastyoliva.wearable.video.presentation.content.UiState
import com.bastyoliva.wearable.video.usecase.CheckPermissionsUseCase
import com.bastyoliva.wearable.video.usecase.GetMediaUseCase
import com.bastyoliva.wearable.video.usecase.OpenFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val getMediaUseCase: GetMediaUseCase,
    private val checkPermissionsUseCase: CheckPermissionsUseCase,
    private val openFileUseCase: OpenFileUseCase
) : ViewModel() {

    var state by mutableStateOf<UiState>(UiState.Loading)
        private set

    var permissionDialogState by mutableStateOf(PermissionDialogState.HIDE)
        private set


    fun onEvent(event: Event) {
        when (event) {

            is Event.OnNavigateBack -> {

            }
            is Event.OnMediaClick -> {

            }
            is Event.OnLoad -> {
                val hasPermissions = checkPermissionsUseCase()

                if (hasPermissions){
                    permissionDialogState = PermissionDialogState.HIDE
                    loadMedia()
                } else {
                    permissionDialogState = PermissionDialogState.SHOW
                }

            }

            is Event.OnOpenFile -> {
                openFileUseCase(event.uri)
            }
        }
    }

    private fun loadMedia() {
        viewModelScope.launch {
            state = UiState.Loading
            getMediaUseCase()
                .onSuccess { list ->
                    state = UiState.Success(mediaItems = list)
                }
                .onFailure { e ->
                    state = UiState.Failed(e)
                }
        }
    }

}
