package com.bastyoliva.wearable.video.data

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String?,
    val isVideo: Boolean
)