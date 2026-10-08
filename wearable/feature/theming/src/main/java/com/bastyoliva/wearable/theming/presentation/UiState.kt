package com.bastyoliva.wearable.theming.presentation

import androidx.compose.ui.graphics.Color

sealed interface UiState {

    data object Loading : UiState
    data class Success(
        val colors: List<Color> = emptyList(),
        val selectedColor: Color? = null
    ) : UiState
    data class Failed (
        val e: Throwable
    ) : UiState
}