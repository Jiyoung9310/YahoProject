package com.android.yaho.local

import android.content.SharedPreferences
import androidx.core.content.edit

interface YahoPreference {
    var userId: String?
    var selectedMountainId: Int
    var runningTimeStamp: Long
    var runningTimeCount: Long
}

class YahoPreferenceImpl(
    private val preferences: SharedPreferences
) : YahoPreference {
    companion object {
        private const val KEY_USER_ID = "yaho_uid"
        private const val KEY_SELECTED_MOUNTAIN_ID = "selected_mountain_id"
        private const val KEY_RUNNING_TIME_STAMP = "running_time_stamp"
        private const val KEY_RUNNING_TIME_COUNT = "running_time_count"
    }

    override var userId: String?
        get() = preferences.getString(KEY_USER_ID, null)
        set(value) { preferences.edit { putString(KEY_USER_ID, value) } }

    override var selectedMountainId: Int
        get() = preferences.getInt(KEY_SELECTED_MOUNTAIN_ID, 0)
        set(value) { preferences.edit { putInt(KEY_SELECTED_MOUNTAIN_ID, value)} }
    override var runningTimeStamp: Long
        get() = preferences.getLong(KEY_RUNNING_TIME_STAMP, 0)
        set(value) { preferences.edit { putLong(KEY_RUNNING_TIME_STAMP, value)} }
    override var runningTimeCount: Long
        get() = preferences.getLong(KEY_RUNNING_TIME_COUNT, 0)
        set(value) { preferences.edit { putLong(KEY_RUNNING_TIME_COUNT, value) }}
}