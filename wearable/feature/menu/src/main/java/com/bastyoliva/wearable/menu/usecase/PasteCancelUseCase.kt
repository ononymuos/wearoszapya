package com.bastyoliva.wearable.menu.usecase

import com.bastyoliva.wearable.data.repository.ClipboardRepository
import javax.inject.Inject

class PasteCancelUseCase @Inject constructor(
    private val clipboardRepository: ClipboardRepository
) {
     operator fun invoke() {
        return clipboardRepository.cancel()
    }
}