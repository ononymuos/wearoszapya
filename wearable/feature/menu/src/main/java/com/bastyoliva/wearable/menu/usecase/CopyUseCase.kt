package com.bastyoliva.wearable.menu.usecase

import com.bastyoliva.wearable.data.repository.ClipboardRepository
import javax.inject.Inject

class CopyUseCase @Inject constructor(
    private val clipboardRepository: ClipboardRepository
) {
    operator fun invoke(paths: List<String>) {
        return clipboardRepository.copy(paths)
    }
}