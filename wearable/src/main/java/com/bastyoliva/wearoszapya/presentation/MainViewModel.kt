package com.bastyoliva.wearoszapya.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastyoliva.wearable.design.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MainViewState(
    val isLoading: Boolean = true,
    val themeColor: Color? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    themeManager: ThemeManager
) : ViewModel() {

    private val _viewState = MutableStateFlow(MainViewState())
    val viewState = _viewState.asStateFlow()

    init {
        themeManager.themeColor
            .onEach { color ->
                _viewState.update { it.copy(isLoading = false, themeColor = color) }
            }
            .launchIn(viewModelScope)
    }

    val dynamicColorsEnabled = themeManager.dynamicColorsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

}
