package com.bastyoliva.wearoszapya.common.viewers

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object Constants {
    const val THEME_MANAGER_DATASTORE = "theme_manager"
    val COLOR_INT_ARGB = intPreferencesKey("color_int_argb")

    val DYNAMIC_COLORS_ENABLED = booleanPreferencesKey("dyn_colors_enabled")

}