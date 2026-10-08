package com.bastyoliva.wearable.file_list.usecase

import com.bastyoliva.wearable.navigation.Navigator
import com.bastyoliva.wearable.navigation.Routes
import javax.inject.Inject

class NavigateToPathUseCase @Inject constructor(
    private val navigator: Navigator
) {
    operator fun invoke(path: String) {
        return navigator.navigate(Routes.FilesList(path))
    }
}