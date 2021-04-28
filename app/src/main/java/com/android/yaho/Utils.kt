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

fun List<Location>?.getLocationResultText(): String? {
    if (this == null || this.isEmpty()) {
        return "no location"
    }
    val sb = StringBuilder()
    for (location in this) {
        sb.append("(")
        sb.append("위도 : ")
        sb.append(location.latitude)
        sb.append(", ")
        sb.append("경도 : ")
        sb.append(location.longitude)
        sb.append(", ")
        sb.append("고도 : ")
        sb.append(location.altitude)
        sb.append(", ")
        sb.append("속도 : ")
        sb.append(location.speed)
        sb.append(")")
        sb.append("\n")
    }
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