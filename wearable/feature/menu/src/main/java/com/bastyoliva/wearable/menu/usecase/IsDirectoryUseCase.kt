package com.bastyoliva.wearable.menu.usecase

import com.bastyoliva.wearable.data.repository.FileManagerRepository
import javax.inject.Inject

class IsDirectoryUseCase @Inject constructor(
    private val fileManagerRepository: FileManagerRepository
) {
    suspend operator fun invoke(path: String): Boolean {
        return fileManagerRepository.isDirectory(path)
    }
}

