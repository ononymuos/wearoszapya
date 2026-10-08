package com.bastyoliva.wearable.data.repository

interface ClipboardRepository {

    fun cut(paths: List<String>)

    fun copy(paths: List<String>)

    fun cancel()

    suspend fun insertTo(path: String): Boolean

    var operation: ClipboardOperation?


}

enum class ClipboardOperation { CUT, COPY }