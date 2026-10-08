package com.bastyoliva.wearable.rename.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastyoliva.wearable.rename.usecase.GetFileNameUseCase
import com.bastyoliva.wearable.rename.usecase.NavigateBackUseCase
import com.bastyoliva.wearable.rename.usecase.RenameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RenameViewModel @Inject constructor(
    private val getFileNameUseCase: GetFileNameUseCase,
    private val renameUseCase: RenameUseCase,
    private val navigateBackUseCase: NavigateBackUseCase
) : ViewModel() {


    var state by mutableStateOf<UiState>(UiState.Loading)
        private set

    fun onEvent(event: Event) {
        when (event) {
           is   Event.OnGetFileName -> {
               getFileNameUseCase(event.path)
                   .onSuccess { name ->
                       state = UiState.Success(
                           path = event.path,
                           fileName = name,
                           newFileName = name
                       )
                   }
                   .onFailure {
                       state = UiState.Failed(it)
                   }
           }
            is Event.OnNewFileNameChanged -> {
                val currentState = state
                if (currentState is UiState.Success) {
                    state = currentState.copy(newFileName = event.name)
                }
            }

            is Event.OnRename -> {
                viewModelScope.launch {
                    renameUseCase(event.path, event.newName)
                        .onSuccess { navigateBackUseCase() }
                        .onFailure { state = UiState.Failed(it) }
                }
            }

            is Event.OnNavigateBack -> {
                navigateBackUseCase()
            }
        }
    }


}
