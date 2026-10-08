package com.bastyoliva.wearable.home.presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.dialog.Dialog
import com.bastyoliva.wearable.design.components.basic_screens.ContentLoadingDefaultScreen
import com.bastyoliva.wearable.menu.MenuRoute

@Composable
fun HomeScreen(onEvent: (Event) -> Unit, uiState: UiState, menuState: MenuState) {

    Dialog(
        showDialog = menuState is MenuState.Show,
        onDismissRequest = {
            onEvent(Event.OnHideMenu)
        }
    ) {
        if (menuState is MenuState.Show){
            MenuRoute(
                paths = menuState.paths,
                onDismissRequest = {
                    onEvent(Event.OnHideMenu)
                },
                mode = menuState.menuMode
            )
        }
    }

    when(uiState){
        is UiState.Loading -> ContentLoadingDefaultScreen()
        is UiState.Success -> ContentSuccess(uiState.homeItems, uiState.pinnedItems, onEvent)
        is UiState.Failed -> {}
     }

}
