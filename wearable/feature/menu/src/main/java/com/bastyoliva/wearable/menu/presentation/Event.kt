package com.bastyoliva.wearable.menu.presentation

import com.bastyoliva.wearable.menu.MenuMode

sealed class Event {

    object OnNavigateBack : Event()

    data class OnNavigateToRename(val path: String) : Event()

    data class OnNavigateToDelete(val paths: List<String>) : Event()

    data class OnHeaderClick(val path: String) : Event()
    data class OnNavigateToNewDirectory(val path: String) : Event()

    data class OnGetMenuActions(val paths: List<String>, val mode: MenuMode) : Event()

    data class OnFileClick(val path: String) : Event()

    data class OnPin(val paths: List<String>) : Event()

    data class OnUnpin(val paths: List<String>) : Event()
    data class OnDirectoryClick(val path: String) : Event()

    data class OnCut(val paths: List<String>) : Event()

    data class OnCopy(val paths: List<String>) : Event()

    data class OnPaste(val path: String) : Event()

    data class OnCancelPaste(val path: String) : Event()




}
