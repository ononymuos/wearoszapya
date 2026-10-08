package com.bastyoliva.wearable.images.data

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String?,
    val isVideo: Boolean
)