package com.bastyoliva.wearable.home.presentation

import com.bastyoliva.wearable.data.model.PinnedItem
import com.bastyoliva.wearable.home.model.HomeItem

sealed interface UiState {
    data object Loading : UiState
    data class Success(
        val homeItems: List<HomeItem>,
        val pinnedItems: List<PinnedItem>
    ) : UiState
    data class Failed (
        val e: Throwable
    ) : UiState
}