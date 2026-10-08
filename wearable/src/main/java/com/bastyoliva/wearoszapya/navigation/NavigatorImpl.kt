package com.bastyoliva.wearoszapya.navigation

import com.bastyoliva.wearable.navigation.NavigationAction
import com.bastyoliva.wearable.navigation.Navigator
import com.bastyoliva.wearable.navigation.Routes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigatorImpl @Inject constructor() : Navigator {

    private val _navigationActions = Channel<NavigationAction>()
    override val navigationActions = _navigationActions.receiveAsFlow()

    override fun navigate(route: Routes) {
        _navigationActions.trySend(NavigationAction.Navigate(route))
    }

    override fun navigateUp() {
        _navigationActions.trySend(NavigationAction.NavigateUp)
    }

    override fun navigateAndClearBackStack(
        route: Routes,
        popupTo: Routes,
        inclusive: Boolean
    ) {
        _navigationActions.trySend(NavigationAction.NavigateAndClearBackStack(route, popupTo, inclusive))
    }

}