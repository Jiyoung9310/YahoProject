package com.climbing.yaho.local

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

interface YahoPreference {
    var userId: String?
    var selectedMountainId: Int
    var runningTimeStamp: Long
    var runningTimeCount: Long
    var restTimeCount: Long
    var isActive: Boolean
    var isSubscribing: Boolean
    var isConfirm: Boolean
    var noMoreAdsPopupToday: Long
    fun clearSelectedMountain()
}

class YahoPreferenceImpl @Inject constructor(
    private val preferences: SharedPreferences
) : YahoPreference {
    companion object {
        private const val KEY_USER_ID = "yaho_uid"
        private const val KEY_SELECTED_MOUNTAIN_ID = "selected_mountain_id"
        private const val KEY_RUNNING_TIME_STAMP = "running_time_stamp"
        private const val KEY_RUNNING_TIME_COUNT = "running_time_count"
        private const val KEY_REST_TIME_COUNT = "rest_time_count"
        private const val KEY_IS_ACTIVE = "is_active"
        private const val KEY_IS_SUBSCRIBING = "is_subscribing"
        private const val KEY_IS_CONFIRM = "is_confirm"
        private const val KEY_NO_MORE_ADS_POPUP_TODAY = "no_more_ads_popup_today"
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
    override var restTimeCount: Long
        get() = preferences.getLong(KEY_REST_TIME_COUNT, 0)
        set(value) { preferences.edit { putLong(KEY_REST_TIME_COUNT, value) }}
    override var isActive: Boolean
        get() = preferences.getBoolean(KEY_IS_ACTIVE, true)
        set(value) { preferences.edit { putBoolean(KEY_IS_ACTIVE, value) }}
    override var isSubscribing: Boolean
        get() = preferences.getBoolean(KEY_IS_SUBSCRIBING, false)
        set(value) { preferences.edit{ putBoolean(KEY_IS_SUBSCRIBING, value) }}
    override var isConfirm: Boolean
        get() = preferences.getBoolean(KEY_IS_CONFIRM, false)
        set(value) { preferences.edit { putBoolean(KEY_IS_CONFIRM, value) }}

    override var noMoreAdsPopupToday: Long
        get() = preferences.getLong(KEY_NO_MORE_ADS_POPUP_TODAY, 0)
        set(value) { preferences.edit { putLong(KEY_NO_MORE_ADS_POPUP_TODAY, value) }}

    override fun clearSelectedMountain() {
        selectedMountainId = 0
        runningTimeStamp = 0
        runningTimeCount = 0
        restTimeCount = 0
        isActive = true
    }
}