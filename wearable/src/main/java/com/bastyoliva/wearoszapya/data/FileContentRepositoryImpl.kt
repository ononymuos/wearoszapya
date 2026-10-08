package com.bastyoliva.wearoszapya.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.bastyoliva.wearable.data.repository.FileContentRepository
import java.io.File
import javax.inject.Inject

class FileContentRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver
) : FileContentRepository {

    override suspend fun getTextContent(uri: Uri): String {
        return contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.readText()
            ?: ""
    }

    override suspend fun getFileName(uri: Uri): String {
        var name: String? = null

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }

        if (name == null && uri.scheme == "file") {
            name = File(uri.path!!).name
        }

        return name ?: "unknown"
    }
}
