package com.bastyoliva.wearable.images.usecase

import com.bastyoliva.wearable.data.repository.FileManagerRepository
import javax.inject.Inject

class CheckPermissionsUseCase @Inject constructor(
    private val fileManagerRepository: FileManagerRepository
) {

    operator fun invoke(): Boolean {
        return fileManagerRepository.hasImagesAccess()
    }

}
