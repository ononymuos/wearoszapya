package com.bastyoliva.wearable.home.presentation

import com.bastyoliva.wearable.menu.MenuMode

sealed interface MenuState {
    data object Hide : MenuState
    data class Show(
        val paths: List<String>,
        val menuMode: MenuMode,
    ) : MenuState

}