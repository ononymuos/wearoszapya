package com.bastyoliva.wearable.music.data

import android.net.Uri

data class MusicItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
)