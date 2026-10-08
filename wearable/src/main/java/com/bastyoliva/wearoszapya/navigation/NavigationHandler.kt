package com.bastyoliva.wearoszapya.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.bastyoliva.wearable.navigation.NavigationAction
import com.bastyoliva.wearable.navigation.Navigator

@Composable
fun NavigationHandler(
    navigator: Navigator,
    navController: NavHostController
) {
    LaunchedEffect(navigator) {
        navigator.navigationActions.collect { action ->

            Log.e("action", action.toString())

            when (action) {
                is NavigationAction.Navigate -> {
                    navController.navigate(action.route)
                }
                is NavigationAction.NavigateUp -> {
                    navController.navigateUp()
                }
                is NavigationAction.NavigateAndClearBackStack -> {
                    navController.navigate(action.route) {
                        popUpTo(action.popupTo) { inclusive = action.inclusive }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}