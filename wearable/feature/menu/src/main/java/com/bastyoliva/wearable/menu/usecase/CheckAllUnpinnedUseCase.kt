package com.bastyoliva.wearable.menu.usecase

import com.bastyoliva.wearable.data.repository.PinnedRepository
import javax.inject.Inject

class CheckAllUnpinnedUseCase @Inject constructor(
    private val pinnedRepository: PinnedRepository
) {
    suspend operator fun invoke(paths: List<String>): Boolean =
        paths.all { !pinnedRepository.isPinned(it) }
}