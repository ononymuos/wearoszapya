package com.bastyoliva.wearable.music.usecase

import android.content.ContentResolver
import android.content.ContentUris
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import com.bastyoliva.wearable.music.data.MusicItem
import javax.inject.Inject

class GetMusicUseCase @Inject constructor(
    private val contentResolver: ContentResolver
) {

    operator fun invoke(): Result<List<MusicItem>> {
        return try {
            val result = queryAudios()
            val finalList = result.sortedByDescending { it.id }
            Result.success(finalList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun queryAudios(): List<MusicItem> {
        val list = mutableListOf<MusicItem>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME
        )

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val args = Bundle().apply {
                putStringArray(
                    ContentResolver.QUERY_ARG_SORT_COLUMNS,
                    arrayOf(MediaStore.Audio.Media._ID)
                )
                putInt(
                    ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                )
            }
            contentResolver.query(uri, projection, args, null)
        } else {
            val sort = "${MediaStore.Audio.Media._ID} DESC"
            contentResolver.query(uri, projection, null, null, sort)
        }

        cursor?.use { c ->
            val idIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

            while (c.moveToNext()) {
                val id = c.getLong(idIndex)
                val name = c.getString(nameIndex)

                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                list.add(
                    MusicItem(
                        id = id,
                        uri = contentUri,
                        displayName = name,
                    )
                )
            }
        }

        return list
    }
}
