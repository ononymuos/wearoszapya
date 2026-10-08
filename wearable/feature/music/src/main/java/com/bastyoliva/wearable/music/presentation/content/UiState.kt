package com.bastyoliva.wearable.music.presentation.content

import com.bastyoliva.wearable.music.data.MusicItem

sealed interface UiState {
    data object Loading : UiState
    data class Success(
        val musicItems: List<MusicItem>
    ) : UiState
    data class Failed (
        val e: Throwable
    ) : UiState
}