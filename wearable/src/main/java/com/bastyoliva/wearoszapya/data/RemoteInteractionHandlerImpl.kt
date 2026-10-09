package com.bastyoliva.wearoszapya.data

import android.content.Context
import android.content.Intent
import androidx.concurrent.futures.await
import androidx.core.net.toUri
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.bastyoliva.wearable.data.repository.RemoteInteractionHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
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

    override suspend fun openAppOnPhone(): Result<Unit> {
        var remoteStarted = false

        // 1. Send deep link intent via RemoteActivityHelper
        if (helper != null) {
            try {
                helper?.startRemoteActivity(
                    Intent(Intent.ACTION_VIEW)
                        .addCategory(Intent.CATEGORY_BROWSABLE)
                        .setData(android.net.Uri.parse(com.bastyoliva.wearoszapya.data.TurboConstants.URI_SCHEME_OPEN))
                )?.await()
                remoteStarted = true
            } catch (e: Throwable) {
                android.util.Log.w("RemoteInteraction", "startRemoteActivity failed: ${e.message}")
            }
        }

        // 2. Broadcast message /zapya/open-app to all connected phone nodes
        var messageSent = false
        try {
            val nodeClient = com.google.android.gms.wearable.Wearable.getNodeClient(context)
            val nodes = nodeClient.connectedNodes.await()
            val messageClient = com.google.android.gms.wearable.Wearable.getMessageClient(context)
            for (node in nodes) {
                messageClient.sendMessage(node.id, com.bastyoliva.wearoszapya.data.TurboConstants.PATH_OPEN_APP, ByteArray(0)).await()
                messageSent = true
            }
        } catch (e: Throwable) {
            android.util.Log.w("RemoteInteraction", "sendMessage failed: ${e.message}")
        }

        return if (remoteStarted || messageSent) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("No connected phone found"))
        }
    }
}
