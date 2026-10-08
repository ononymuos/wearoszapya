package com.bastyoliva.wearable.video.usecase

import android.net.Uri
import com.bastyoliva.wearable.data.repository.FileInteractionHandler
import javax.inject.Inject

class OpenFileUseCase @Inject constructor(
    private val fileInteractionHandler: FileInteractionHandler
) {
    operator fun invoke(uri: Uri) {
        return fileInteractionHandler.openFileByUri(uri)
    }
}