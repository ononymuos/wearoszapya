package com.bastyoliva.wearoszapya.data

import android.content.Context
import android.content.Intent
import androidx.concurrent.futures.await
import androidx.core.net.toUri
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.bastyoliva.wearable.data.repository.RemoteInteractionHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RemoteInteractionHandlerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : RemoteInteractionHandler {

    private val helper: RemoteActivityHelper? by lazy {
        try {
            RemoteActivityHelper(context)
        } catch (e: Throwable) {
            null
        }
    }

    override suspend fun openRemoteLink(url: String): Result<Unit> {
        val h = helper ?: return Result.failure(Exception("Remote activities not supported on this device"))
        return try {
            h.startRemoteActivity(
                Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(url.toUri())
            ).await()
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
