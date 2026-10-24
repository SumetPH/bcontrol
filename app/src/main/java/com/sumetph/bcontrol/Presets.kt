package com.sumetph.bcontrol

import android.content.Context

object Presets {
    private val DEFAULTS = intArrayOf(10, 60, 150, 255)
    val SLOTS = DEFAULTS.indices

    private fun prefs(ctx: Context) = ctx.getSharedPreferences("presets", Context.MODE_PRIVATE)

    fun get(ctx: Context, slot: Int): Int = prefs(ctx).getInt("slot$slot", DEFAULTS[slot])

    fun save(ctx: Context, slot: Int, value: Int) = prefs(ctx).edit().putInt("slot$slot", value).apply()
}
