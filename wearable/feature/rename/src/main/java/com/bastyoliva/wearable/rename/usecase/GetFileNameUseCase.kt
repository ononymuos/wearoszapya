package com.bastyoliva.wearable.rename.usecase

import com.bastyoliva.wearable.data.repository.FileManagerRepository
import javax.inject.Inject

class GetFileNameUseCase @Inject constructor(
    private val fileManagerRepository: FileManagerRepository
) {
    operator fun invoke(path: String): Result<String> {
        return fileManagerRepository.getFileName(path)
    }
}