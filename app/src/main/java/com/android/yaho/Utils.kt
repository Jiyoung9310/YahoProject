package com.android.yaho

import android.location.Location

private const val KEY_LOCATION_UPDATES_RESULT = "location-update-result"

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