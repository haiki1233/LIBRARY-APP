package com.example.library_mobile.data.local

import android.content.Context
import android.content.SharedPreferences

class ReadingPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ===== READ MODE =====
    var readMode: ReadMode
        get() = ReadMode.valueOf(prefs.getString(KEY_READ_MODE, ReadMode.IMAGE.name)!!)
        set(value) = prefs.edit().putString(KEY_READ_MODE, value.name).apply()

    // ===== FONT SIZE =====
    var fontSize: Float
        get() = prefs.getFloat(KEY_FONT_SIZE, 16f)
        set(value) = prefs.edit().putFloat(KEY_FONT_SIZE, value.coerceIn(12f, 28f)).apply()

    // ===== DARK MODE =====
    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, true) // Mặc định dark
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    // ===== LINE SPACING =====
    var lineSpacing: Float
        get() = prefs.getFloat(KEY_LINE_SPACING, 1.6f)
        set(value) = prefs.edit().putFloat(KEY_LINE_SPACING, value.coerceIn(1.2f, 2.4f)).apply()

    // ===== FONT FAMILY =====
    var fontFamily: FontFamily
        get() = FontFamily.valueOf(prefs.getString(KEY_FONT_FAMILY, FontFamily.DEFAULT.name)!!)
        set(value) = prefs.edit().putString(KEY_FONT_FAMILY, value.name).apply()

    // ===== BRIGHTNESS =====
    var brightness: Int
        get() = prefs.getInt(KEY_BRIGHTNESS, -1) // -1 = auto
        set(value) = prefs.edit().putInt(KEY_BRIGHTNESS, value).apply()

    enum class ReadMode { IMAGE, TEXT }
    enum class FontFamily { DEFAULT, SERIF, MONOSPACE }

    companion object {
        private const val PREFS_NAME       = "reading_prefs"
        private const val KEY_READ_MODE    = "read_mode"
        private const val KEY_FONT_SIZE    = "font_size"
        private const val KEY_DARK_MODE    = "dark_mode"
        private const val KEY_LINE_SPACING = "line_spacing"
        private const val KEY_FONT_FAMILY  = "font_family"
        private const val KEY_BRIGHTNESS   = "brightness"
    }
}