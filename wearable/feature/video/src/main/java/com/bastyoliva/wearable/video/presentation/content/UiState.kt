package com.bastyoliva.wearable.video.presentation.content

import com.bastyoliva.wearable.video.data.MediaItem

sealed interface UiState {
    data object Loading : UiState
    data class Success(
        val mediaItems: List<MediaItem>
    ) : UiState
    data class Failed (
        val e: Throwable
    ) : UiState
}