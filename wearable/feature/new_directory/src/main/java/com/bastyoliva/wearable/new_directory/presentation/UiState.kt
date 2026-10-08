package com.bastyoliva.wearable.new_directory.presentation

sealed interface UiState {
    data object Loading : UiState
    data class Success(
        val path: String,
        val fileName: String = "",
    ) : UiState {
        fun isSaveEnabled(): Boolean = (fileName.isNotBlank())
    }


    data class Failed (
        val e: Throwable
    ) : UiState
}