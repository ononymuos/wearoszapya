package com.bastyoliva.wearable.settings.usecase

import com.bastyoliva.wearable.data.repository.RemoteInteractionHandler
import javax.inject.Inject

class OpenAppOnPhoneUseCase @Inject constructor(
    private val remoteInteractionHandler: RemoteInteractionHandler
) {
    suspend operator fun invoke(): Result<Unit> {
        return remoteInteractionHandler.openAppOnPhone()
    }
}
