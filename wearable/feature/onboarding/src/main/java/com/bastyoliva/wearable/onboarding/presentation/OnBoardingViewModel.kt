package com.bastyoliva.wearable.onboarding.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastyoliva.wearable.onboarding.presentation.content.DialogState
import com.bastyoliva.wearable.onboarding.presentation.content.UiState
import com.bastyoliva.wearable.onboarding.usecase.OpenLinkOnPhoneUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    val openLinkOnPhoneUseCase: OpenLinkOnPhoneUseCase,
) : ViewModel() {


    var state by mutableStateOf(UiState.FAILED)
        private set

    var dialogState by mutableStateOf(DialogState.CLOSED)
        private set


    fun onEvent(event: Event) {
        when (event) {
            is Event.OnOpenLinkOnPhone -> {
                openLinkOnPhone()
            }

            is Event.CloseDialog -> {
                dialogState = DialogState.CLOSED
            }

            is Event.ShowDialog -> {
                dialogState = if (event.isSuccessful){
                    DialogState.SUCCESS
                } else {
                    DialogState.FAILED
                }
            }
        }
    }


    private fun openLinkOnPhone(){
        viewModelScope.launch {
            openLinkOnPhoneUseCase("https://github.com/bastyoliva/wearoszapya/tree/master?tab=readme-ov-file#how-to-manually-grant-access-to-files")
                .onSuccess {
                    onEvent(Event.ShowDialog(isSuccessful = true))
                }
                .onFailure {
                    onEvent(Event.ShowDialog(isSuccessful = false))
                }

        }

    }

}
