package com.bastyoliva.wearable.menu.usecase

import com.bastyoliva.wearable.data.repository.ClipboardRepository
import javax.inject.Inject

class PasteHereUseCase @Inject constructor(
    private val clipboardRepository: ClipboardRepository
) {
    suspend operator fun invoke(path: String): Boolean {
        return clipboardRepository.insertTo(path)
    }
}