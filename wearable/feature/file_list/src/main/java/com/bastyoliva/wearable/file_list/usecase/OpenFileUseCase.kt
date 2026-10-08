package com.bastyoliva.wearable.file_list.usecase

import com.bastyoliva.wearable.data.repository.FileInteractionHandler
import java.io.File
import javax.inject.Inject

class OpenFileUseCase @Inject constructor(
    private val fileInteractionHandler: FileInteractionHandler
) {
    operator fun invoke(file: File) {
        return fileInteractionHandler.openFileByPath(file.absolutePath)
    }
}