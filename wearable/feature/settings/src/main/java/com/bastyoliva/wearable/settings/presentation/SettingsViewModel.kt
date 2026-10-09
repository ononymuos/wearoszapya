package com.bastyoliva.wearable.settings.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastyoliva.wearable.navigation.Routes
import com.bastyoliva.wearable.settings.usecase.NavigateToUseCase
import com.bastyoliva.wearable.settings.usecase.OpenLinkOnPhoneUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val navigateToUseCase: NavigateToUseCase,
    private val openLinkOnPhoneUseCase: OpenLinkOnPhoneUseCase,
    private val openAppOnPhoneUseCase: com.bastyoliva.wearable.settings.usecase.OpenAppOnPhoneUseCase
) : ViewModel() {

    var dialogState by mutableStateOf(DialogState.CLOSED)
        private set

    fun onEvent(event: Event) {
        when (event) {

            is Event.OnNavigateBack -> {

            }

            Event.OnOpenPhoneApp -> {
                dialogState = DialogState.OPENING_PHONE
                viewModelScope.launch {
                    openAppOnPhoneUseCase().onSuccess {
                        onEvent(Event.ShowDialog(isSuccessful = true))
                    }.onFailure {
                        onEvent(Event.ShowDialog(isSuccessful = false))
                    }
                }
            }

            Event.OnOpenTurboBoost -> {
                dialogState = DialogState.TURBO_INFO
            }

            Event.OnNavigateToGitRepo -> {
                openLinkOnPhone("https://github.com/bastyoliva/wearoszapya")
            }

            Event.OnOpenEmail -> {
                openLinkOnPhone("mailto:basty.oliva2011@gmail.com")
            }

            Event.OnOpenDonate -> {
                openLinkOnPhone("https://github.com/bastyoliva/wearoszapya#donations")
            }

            Event.OnNavigateToTheming -> {
                navigateToUseCase(routes = Routes.Theming)
            }

            is Event.CloseDialog -> {
                dialogState = DialogState.CLOSED
            }

            is Event.ShowDialog -> {
                dialogState = if (event.isSuccessful) {
                    DialogState.SUCCESS
                } else {
                    DialogState.FAILED
                }
            }
        }
    }

    private fun openLinkOnPhone(url: String) {
        viewModelScope.launch {
            openLinkOnPhoneUseCase(url).onSuccess {
                    onEvent(Event.ShowDialog(isSuccessful = true))
                }.onFailure {
                    onEvent(Event.ShowDialog(isSuccessful = false))
                }

        }

    }

}
