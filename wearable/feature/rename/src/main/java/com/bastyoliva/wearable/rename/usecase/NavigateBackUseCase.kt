package com.bastyoliva.wearable.rename.usecase

import com.bastyoliva.wearable.navigation.Navigator
import javax.inject.Inject

class NavigateBackUseCase @Inject constructor(
    private val navigator: Navigator
) {
    operator fun invoke() {
        navigator.navigateUp()
    }
}