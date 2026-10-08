package com.bastyoliva.wearable.new_directory.presentation

sealed class Event {

    data class OnNameChanged(val name: String) : Event()

    object OnNavigateBack : Event()
    data class OnCreateNewDir(val path: String, val newName: String) : Event()

}
