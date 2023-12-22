package com.quickblox.qb_qmunicate.presentation.theme_manager

import android.content.Context
import android.content.res.Configuration
import com.quickblox.android_ui_kit.presentation.theme.DarkUiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.LightUIKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme

object ThemeManager {
    fun checkModeAndGetUIKitTheme(context: Context): UiKitTheme {
        return if (isNightMode(context)) {
            DarkUiKitTheme()
        } else {
            LightUIKitTheme()
        }
    }

    private fun isNightMode(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }
}