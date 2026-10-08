package com.bastyoliva.wearable.design.theme

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow

interface ThemeManager {

    suspend fun setThemeColor(color: Color?)

    val themeColor: Flow<Color?>

    val dynamicColorsEnabled: Flow<Boolean>

    suspend fun setDynamicColorsEnabled(enabled: Boolean)
}

