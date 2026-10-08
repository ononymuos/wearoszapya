package com.bastyoliva.wearable.text_viewer.presentation

sealed class Event {

    data class OnGetFileTextContent(val uriString: String) : Event()

    object OnExit : Event()

}