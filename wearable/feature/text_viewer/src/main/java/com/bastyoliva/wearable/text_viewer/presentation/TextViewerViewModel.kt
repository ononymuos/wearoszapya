package com.bastyoliva.wearable.text_viewer.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastyoliva.wearable.text_viewer.usecase.GetFileNameUseCase
import com.bastyoliva.wearable.text_viewer.usecase.GetFileTextContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextViewerViewModel @Inject constructor(
    private val getFileTextContentUseCase: GetFileTextContentUseCase,
    private val getFileNameUseCase: GetFileNameUseCase
) : ViewModel() {

    var state by mutableStateOf<UiState>(UiState.Loading)
        private set

    fun onEvent(event: Event) {
        when (event) {
            is Event.OnGetFileTextContent -> {
                getFileTextContent(event.uriString)
            }

            is Event.OnExit -> {
            }

        }
    }

    private fun getFileTextContent(path: String) {
        viewModelScope.launch {
            state = UiState.Loading
            try {
                val content = getFileTextContentUseCase(path)
                val fileName = getFileNameUseCase(path)
                state = UiState.Success(content, fileName)
            } catch (_: Exception) {
                state = UiState.Failed
            }
        }
    }

}
