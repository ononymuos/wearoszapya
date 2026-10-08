package com.bastyoliva.wearable.new_directory.usecase

import com.bastyoliva.wearable.data.repository.FileManagerRepository
import javax.inject.Inject

class CreateDirUseCase @Inject constructor(
    private val fileManagerRepository: FileManagerRepository
) {
    suspend operator fun invoke(path: String, newName: String): Result<Boolean> {
        return fileManagerRepository.createDirectory(path, newName)
    }
}