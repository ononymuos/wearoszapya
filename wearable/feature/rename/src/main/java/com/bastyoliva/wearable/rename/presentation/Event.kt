package com.bastyoliva.wearable.rename.presentation

sealed class Event {

    data class OnGetFileName(val path: String) : Event()
    data class OnNewFileNameChanged(val name: String) : Event()



    object OnNavigateBack : Event()
    data class OnRename(val path: String, val newName: String) : Event()

}
