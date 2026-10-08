package com.bastyoliva.wearoszapya.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.bastyoliva.wearable.design.theme.ThemeManager
import com.bastyoliva.wearoszapya.common.viewers.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemeManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ThemeManager {

    val Context.themeManagerDataStore by preferencesDataStore(
        name = Constants.THEME_MANAGER_DATASTORE
    )

    private val dataStore = context.themeManagerDataStore

    override suspend fun setThemeColor(color: Color?) {
        dataStore.edit { prefs ->
            if (color == null) {
                prefs.remove(Constants.COLOR_INT_ARGB)
            } else {
                prefs[Constants.COLOR_INT_ARGB] = color.toArgb()
            }
        }
    }

    override val themeColor: Flow<Color?> = dataStore.data.map { prefs ->
        val colorInt = prefs[Constants.COLOR_INT_ARGB]
        if (colorInt != null){
            Color(colorInt)
        } else {
            null
        }

    }

    override val dynamicColorsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Constants.DYNAMIC_COLORS_ENABLED] ?: false
    }

    override suspend fun setDynamicColorsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Constants.DYNAMIC_COLORS_ENABLED] = enabled
        }
    }

}
