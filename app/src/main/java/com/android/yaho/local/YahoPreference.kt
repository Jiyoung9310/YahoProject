package com.android.yaho.local

import android.content.SharedPreferences
import androidx.core.content.edit

interface YahoPreference {
    var userId: String?
}

class YahoPreferenceImpl(
    private val preferences: SharedPreferences
) : YahoPreference {
    override var userId: String?
        get() = preferences.getString("yaho_uid", null)
        set(value) { preferences.edit { putString("yaho_uid", value) } }
}