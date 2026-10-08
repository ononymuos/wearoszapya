package com.bastyoliva.wearable.data.repository

import android.net.Uri

interface FileContentRepository {

    suspend fun getTextContent(uri: Uri): String

    suspend fun getFileName(uri: Uri): String
}