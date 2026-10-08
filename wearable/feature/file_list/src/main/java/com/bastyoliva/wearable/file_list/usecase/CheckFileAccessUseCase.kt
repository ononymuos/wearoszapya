package com.bastyoliva.wearable.file_list.usecase

import com.bastyoliva.wearable.data.repository.FileManagerRepository
import javax.inject.Inject

class CheckFileAccessUseCase @Inject constructor(
    private val fileManagerRepository: FileManagerRepository
) {
    operator fun invoke(): Boolean {
        return fileManagerRepository.hasFileAccess()
    }
}