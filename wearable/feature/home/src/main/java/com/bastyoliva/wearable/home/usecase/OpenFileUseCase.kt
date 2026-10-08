package com.bastyoliva.wearable.home.usecase

import com.bastyoliva.wearable.data.repository.FileInteractionHandler
import javax.inject.Inject

class OpenFileUseCase @Inject constructor(
    private val fileInteractionHandler: FileInteractionHandler
) {
    operator fun invoke(path: String) {
        return fileInteractionHandler.openFileByPath(path)
    }
}