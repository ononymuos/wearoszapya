package com.bastyoliva.wearable.delete.usecase

import com.bastyoliva.wearable.data.repository.FileManagerRepository
import javax.inject.Inject

class DeleteUseCase @Inject constructor(
    private val fileManagerRepository: FileManagerRepository
) {
    suspend operator fun invoke(paths: List<String>): Result<Boolean> {
        return fileManagerRepository.delete(paths)
    }
}