package com.bastyoliva.wearable.theming.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastyoliva.wearable.design.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemingViewModel @Inject constructor(
    private val themeManager: ThemeManager
) : ViewModel() {


    val colors = List(10) { index ->
        val hue = index * (360f / 10f)
        Color.hsv(
            hue, 1f, 1f
        )
    }

    var state by mutableStateOf<UiState>(UiState.Loading)
        private set

    init {
        viewModelScope.launch {
            themeManager.themeColor.collect { color ->
                state = UiState.Success(
                    selectedColor = color, colors = colors
                )
            }
        }
    }


    fun onEvent(event: Event) {
        when (event) {

            is Event.OnNavigateBack -> {

            }

            is Event.OnSelectColor -> {
                viewModelScope.launch {
                    themeManager.setThemeColor(event.color)
                }
            }
        }
    }


}
