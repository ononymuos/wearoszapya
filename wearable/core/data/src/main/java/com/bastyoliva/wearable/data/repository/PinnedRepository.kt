package com.bastyoliva.wearable.data.repository

import com.bastyoliva.wearable.data.model.PinnedItem
import kotlinx.coroutines.flow.Flow

interface PinnedRepository {

    suspend fun getPinnedItems(): List<PinnedItem>

    suspend fun pinItem(item: PinnedItem)

    suspend fun unpinItem(item: PinnedItem)

    suspend fun isPinned(path: String): Boolean

    fun getPinnedFlow(): Flow<List<PinnedItem>>



}