package com.bastyoliva.wearable.delete.presentation

sealed interface UiState {
    data object Loading : UiState
    data class Success(
        val paths: List<String>,
    ) : UiState

    data class Failed (
        val e: Throwable
    ) : UiState
}