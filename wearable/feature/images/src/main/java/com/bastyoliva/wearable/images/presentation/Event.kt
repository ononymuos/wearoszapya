package com.bastyoliva.wearable.images.presentation

import android.net.Uri
import com.bastyoliva.wearable.images.data.MediaItem

sealed class Event {


    object OnLoad : Event()

    object OnNavigateBack : Event()
    data class OnMediaClick(val item: MediaItem) : Event()
    data class OnOpenFile(val uri: Uri, val isVideo: Boolean, val displayName: String?) : Event()


}
