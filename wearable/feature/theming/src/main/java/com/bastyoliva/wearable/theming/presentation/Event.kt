package com.bastyoliva.wearable.theming.presentation

import androidx.compose.ui.graphics.Color

sealed class Event {

    object OnNavigateBack : Event()

    data class OnSelectColor(val color: Color?) : Event()


}
