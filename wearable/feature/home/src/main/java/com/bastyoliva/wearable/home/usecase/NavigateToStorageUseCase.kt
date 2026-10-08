package com.bastyoliva.wearable.home.usecase

import com.bastyoliva.wearable.navigation.Navigator
import com.bastyoliva.wearable.navigation.Routes
import javax.inject.Inject

class NavigateToStorageUseCase @Inject constructor(
    private val navigator: Navigator
) {
    operator fun invoke() {
        return navigator.navigate(Routes.FilesList())
    }
}