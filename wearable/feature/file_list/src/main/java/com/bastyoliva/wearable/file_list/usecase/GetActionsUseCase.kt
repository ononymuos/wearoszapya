package com.bastyoliva.wearable.file_list.usecase

import com.bastyoliva.wearable.file_list.presentation.Action
import javax.inject.Inject

class GetActionsUseCase @Inject constructor(
    private val checkIfNavigateUpAvailableUseCase: CheckIfNavigateUpAvailableUseCase,
    private val checkHaveMoreActionsUseCase: CheckHaveMoreActionsUseCase
) {
    suspend operator fun invoke(path: String): List<Action> {
        val actions = mutableListOf<Action>()

        val hasMoreActions = checkHaveMoreActionsUseCase(path)


        val canNavigateUp = checkIfNavigateUpAvailableUseCase(path)

        if (canNavigateUp) {
            actions.add(Action.MOVE_BACK)
        }

        if (hasMoreActions) {
            actions.add(Action.MORE)
        }


        return actions

    }
}