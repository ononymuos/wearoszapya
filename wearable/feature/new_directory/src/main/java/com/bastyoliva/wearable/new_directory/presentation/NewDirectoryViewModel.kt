package com.bastyoliva.wearable.new_directory.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastyoliva.wearable.new_directory.usecase.CreateDirUseCase
import com.bastyoliva.wearable.new_directory.usecase.NavigateBackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewDirectoryViewModel @Inject constructor(
    private val createDirUseCase: CreateDirUseCase,
    private val navigateBackUseCase: NavigateBackUseCase
) : ViewModel() {


    var state by mutableStateOf<UiState>(UiState.Loading)
        private set


    fun setPath(path: String){
        state = UiState.Success(path = path)
    }

    fun onEvent(event: Event) {
        when (event) {

            is Event.OnNameChanged -> {
                val currentState = state
                if (currentState is UiState.Success) {
                    state = currentState.copy(fileName = event.name)
                }
            }

            is Event.OnCreateNewDir -> {
                viewModelScope.launch {
                    createDirUseCase(event.path, event.newName)
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
