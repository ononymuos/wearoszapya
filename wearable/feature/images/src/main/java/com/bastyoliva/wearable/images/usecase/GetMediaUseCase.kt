package com.bastyoliva.wearable.images.usecase

import android.content.ContentResolver
import android.content.ContentUris
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import com.bastyoliva.wearable.images.data.MediaItem
import javax.inject.Inject
import kotlin.collections.plusAssign

class GetMediaUseCase @Inject constructor(
    private val contentResolver: ContentResolver
) {

    operator fun invoke(): Result<List<MediaItem>> {
        return try {
            val result = mutableListOf<MediaItem>()

            result += queryImages()

            val finalList = result.sortedByDescending { it.id }

            Result.success(finalList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun queryImages(): List<MediaItem> {
        val list = mutableListOf<MediaItem>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val args = Bundle().apply {
                putStringArray(
                    ContentResolver.QUERY_ARG_SORT_COLUMNS,
                    arrayOf(MediaStore.Images.Media._ID)
                )
                putInt(
                    ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                )
            }
            contentResolver.query(uri, projection, args, null)
        } else {
            val sort = "${MediaStore.Images.Media._ID} DESC"
            contentResolver.query(uri, projection, null, null, sort)
        }

        cursor?.use { c ->
            val idIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            while (c.moveToNext()) {
                val id = c.getLong(idIndex)
                val name = c.getString(nameIndex)

                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                list.add(
                    MediaItem(
                        id = id,
                        uri = contentUri,
                        displayName = name,
                        isVideo = false
                    )
                )
            }
        }

        return list
    }

}