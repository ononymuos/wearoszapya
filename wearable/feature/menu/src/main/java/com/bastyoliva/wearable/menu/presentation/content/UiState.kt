package com.bastyoliva.wearable.menu.presentation.content

import com.bastyoliva.wearable.menu.presentation.MenuAction

sealed interface UiState {
    data object Loading : UiState

    data object Operation : UiState
    data class Success(
        val name: String,
        val actions: List<MenuAction> = emptyList()
    ) : UiState
    data class Failed (
        val e: Throwable
    ) : UiState
}