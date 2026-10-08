package com.bastyoliva.wearable.design.components.common

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.rotary.RotaryScrollableBehavior
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults

object RotaryUtils {
    val isWearHapticsAvailable: Boolean by lazy {
        try {
            Class.forName("com.google.wear.input.WearHapticFeedbackConstants")
            true
        } catch (_: Exception) {
            false
        }
    }
}

@Composable
fun rememberSafeRotaryScrollableBehavior(
    scrollableState: ScrollableState,
    hapticFeedbackEnabled: Boolean = true
): RotaryScrollableBehavior {
    return RotaryScrollableDefaults.behavior(
        scrollableState = scrollableState,
        hapticFeedbackEnabled = hapticFeedbackEnabled && RotaryUtils.isWearHapticsAvailable
    )
}
