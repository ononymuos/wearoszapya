package com.bastyoliva.wearable.home.usecase

import com.bastyoliva.wearable.navigation.Navigator
import com.bastyoliva.wearable.navigation.Routes
import javax.inject.Inject

class NavigateToUseCase @Inject constructor(
    private val navigator: Navigator
) {
    operator fun invoke(routes: Routes) {
        return navigator.navigate(routes)
    }
}