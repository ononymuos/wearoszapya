package com.bastyoliva.wearable.music.presentation

import com.bastyoliva.wearable.music.data.MusicItem

sealed class Event {


    object OnLoad : Event()

    object OnNavigateBack : Event()
    data class OnMediaClick(val item: MusicItem) : Event()


}
