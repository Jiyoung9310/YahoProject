package com.android.yaho

import android.content.Context
import android.content.res.Resources
import android.location.Location
import android.preference.PreferenceManager
import java.text.DateFormat
import java.util.*

const val KEY_LOCATION_UPDATES_RESULT = "location-update-result"
const val KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates"

/*fun setLocationUpdatesResult(context: Context?, locations: List<Location>?) {
    PreferenceManager.getDefaultSharedPreferences(context)
        .edit()
        .putString(
            KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle(context, locations)
                .toString() + "\n" + getLocationResultText(locations)
        )
        .apply()
}*/

fun Location.getLocationResultText(): String {
    val sb = StringBuilder()
        sb.append("(")
        sb.append("시간 : ")
        sb.append(time)
        sb.append(", ")
        sb.append("위도 : ")
        sb.append(latitude)
        sb.append(", ")
        sb.append("경도 : ")
        sb.append(longitude)
        sb.append(", ")
        sb.append("고도 : ")
        sb.append(altitude)
        sb.append(", ")
        sb.append("속도 : ")
        sb.append(speed)
        sb.append(")")
        sb.append("\n")
    return sb.toString()
}

fun Location.getLocationText(): String {
    return if (this == null) "Unknown location" else "($latitude, $longitude)"
}

fun Context.getLocationTitle(): String {
    return getString(
        R.string.location_updated,
        DateFormat.getDateTimeInstance().format(Date())
    )
}

fun Context?.requestingLocationUpdates(): Boolean {
    return PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, true)
}

/**
 * Stores the location updates state in SharedPreferences.
 * @param requestingLocationUpdates The location updates state.
 */
fun Context?.setRequestingLocationUpdates(requestingLocationUpdates: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(this)
        .edit()
        .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
        .apply()
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Long.secondsToHour() = this / 3600
fun Long.secondsToMinute() = (this % 3600) / 60
fun Long.secondsToSec() = (this % 3600) % 60