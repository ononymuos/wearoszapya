package com.bastyoliva.wearable.image_viewer.presentation

sealed interface UiState {
    data object Loading : UiState
    data class Success(val uriString: String) : UiState
    data object Failed : UiState
}