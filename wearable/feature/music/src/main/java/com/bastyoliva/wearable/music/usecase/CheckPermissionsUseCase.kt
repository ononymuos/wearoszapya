package com.bastyoliva.wearable.music.usecase

import com.bastyoliva.wearable.data.repository.FileManagerRepository
import javax.inject.Inject

class CheckPermissionsUseCase @Inject constructor(
    private val fileManagerRepository: FileManagerRepository
) {

    operator fun invoke(): Boolean {
        return fileManagerRepository.hasAudioAccess()
    }

}
