package com.sumetph.bcontrol

import android.content.ContentResolver
import android.provider.Settings

object Brightness {
    const val MIN = 4
    const val MAX = 255
    const val STEP = 1

    // EMUI restores brightness from this key on screen-on, ignoring screen_brightness.
    private const val EMUI_KEY = "hw_screen_temp_brightness"

    fun get(cr: ContentResolver): Int =
        Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS, MAX / 2)

    /** Throws SecurityException / IllegalArgumentException without WRITE_SETTINGS or if targetSdk > 22. */
    fun set(cr: ContentResolver, value: Int) {
        val v = value.coerceIn(MIN, MAX)
        Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
        Settings.System.putInt(cr, EMUI_KEY, v)
        Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, v)
    }
}
