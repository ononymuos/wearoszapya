package com.bastyoliva.wearable.settings.presentation

sealed class Event {
    object OnNavigateBack : Event()

    object OnNavigateToTheming : Event()

    object OnNavigateToGitRepo : Event()

    object OnOpenEmail : Event()

    object OnOpenDonate : Event()

    object CloseDialog : Event()

    data class ShowDialog (val isSuccessful: Boolean)  : Event()
}
