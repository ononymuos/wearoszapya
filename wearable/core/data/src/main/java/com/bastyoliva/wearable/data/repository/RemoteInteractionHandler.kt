package com.bastyoliva.wearable.data.repository

interface RemoteInteractionHandler {
    suspend fun openRemoteLink(url: String): Result<Unit>
}
