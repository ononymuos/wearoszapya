package com.bastyoliva.wearable.menu.usecase

import com.bastyoliva.wearable.navigation.Navigator
import com.bastyoliva.wearable.navigation.Routes
import javax.inject.Inject

class NavigateToRenameUseCase @Inject constructor(
    private val navigator: Navigator
) {
    operator fun invoke(path: String) {
        navigator.navigate(Routes.Rename(path))
    }
}