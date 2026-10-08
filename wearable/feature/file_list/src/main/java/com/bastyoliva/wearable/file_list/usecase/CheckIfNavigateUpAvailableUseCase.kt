package com.bastyoliva.wearable.file_list.usecase

import com.bastyoliva.wearable.data.repository.FileManagerRepository
import javax.inject.Inject

class CheckIfNavigateUpAvailableUseCase @Inject constructor(
    private val fileManagerRepository: FileManagerRepository
) {
    operator fun invoke(path: String): Boolean {
        return fileManagerRepository.canNavigateUpFrom(
            path = path
        )
    }
}