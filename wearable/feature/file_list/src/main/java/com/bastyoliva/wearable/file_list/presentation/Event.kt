package com.bastyoliva.wearable.file_list.presentation

import com.bastyoliva.wearable.menu.MenuMode
import java.io.File

sealed class Event {

    data class OnGetFileListAtPath(val path: String? = null) : Event()
    data class OnFileClick(val file: File) : Event()

    data class OnDirectoryClick(val path: String) : Event()

    object OnNavigateBack : Event()

    data class OnNavigateToParent(val path: String) : Event()

    data class OnShowMenuFor(val paths: List<String>, val menuMode: MenuMode) : Event()

    object OnHideMenu : Event()




}
