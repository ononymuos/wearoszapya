package com.bastyoliva.wearable.delete.presentation

sealed class Event {

    object OnNavigateBack : Event()
    data class OnDelete(val paths: List<String>) : Event()

}
