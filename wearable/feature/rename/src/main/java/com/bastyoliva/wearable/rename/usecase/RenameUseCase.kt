package com.bastyoliva.wearable.rename.usecase

import com.bastyoliva.wearable.data.repository.FileManagerRepository
import javax.inject.Inject

class RenameUseCase @Inject constructor(
    private val fileManagerRepository: FileManagerRepository
) {
    suspend operator fun invoke(path: String, newName: String): Result<Boolean> {
        return fileManagerRepository.rename(path, newName)
    }
}