package com.bastyoliva.wearable.rename.presentation

sealed interface UiState {
    data object Loading : UiState
    data class Success(
        val path: String,
        val fileName: String,
        val newFileName: String
    ) : UiState {
        fun isSaveEnabled(): Boolean = (newFileName.isNotBlank()) && (newFileName != fileName)
    }


    data class Failed (
        val e: Throwable
    ) : UiState
}