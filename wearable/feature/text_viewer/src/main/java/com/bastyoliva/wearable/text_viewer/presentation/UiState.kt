package com.bastyoliva.wearable.text_viewer.presentation

sealed interface UiState {
    data object Loading : UiState
    data class Success(val content: String?, val fileName: String) : UiState
    data object Failed : UiState
}