package com.bastyoliva.wearable.file_list.usecase

import com.bastyoliva.wearable.data.repository.FileManagerRepository
import javax.inject.Inject

class GetParentUseCase @Inject constructor(
    private val fileManagerRepository: FileManagerRepository
) {
    suspend operator fun invoke(path: String): String? {
        return fileManagerRepository.getParentFilePath(
            path = path
        )
    }
}