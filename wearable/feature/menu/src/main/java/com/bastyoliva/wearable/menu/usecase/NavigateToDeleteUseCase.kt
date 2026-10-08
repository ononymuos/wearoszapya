package com.bastyoliva.wearable.menu.usecase

import com.bastyoliva.wearable.navigation.Navigator
import com.bastyoliva.wearable.navigation.Routes
import javax.inject.Inject

class NavigateToDeleteUseCase @Inject constructor(
    private val navigator: Navigator
) {
    operator fun invoke(paths: List<String>) {
        navigator.navigate(Routes.Delete(paths))
    }
}