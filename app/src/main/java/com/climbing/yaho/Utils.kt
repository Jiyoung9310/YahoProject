package com.climbing.yaho

import android.content.Context
import android.content.res.Resources
import android.location.Location
import android.preference.PreferenceManager
import com.climbing.yaho.R
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
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

fun Long.millisecondsToHourTimeFormat(): String = (this / 1000).secondsToHourTimeFormat()
fun Long.secondsToHourTimeFormat(): String {
    return if(secondsToHour() > 0) {
        String.format("%d시간 %02d분 %02d초", secondsToHour(), secondsToMinute(), secondsToSec())
    } else {
        String.format("%02d분 %02d초", secondsToMinute(), secondsToSec())
    }
}

fun Long.secondsToMinuteTimeFormat(): String =
    String.format("%02d분 %02d초", secondsToMinute(), secondsToSec())

const val FULL_FORMAT_DATE_PATTERN = "yyyy.MM.dd (E)"
fun convertFullFormatDate(milliseconds: Long = 0) : String {
    val date = Date(milliseconds)
    val dateFormat = SimpleDateFormat(FULL_FORMAT_DATE_PATTERN, Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }
    return dateFormat.format(date)
}

const val HOUR_TIME_FORMAT_PATTERN = "a h시 mm분"
fun convertHourTimeFormat(milliseconds: Long = 0) : String {
    val date = Date(milliseconds)
    val dateFormat = SimpleDateFormat(HOUR_TIME_FORMAT_PATTERN, Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }
    return dateFormat.format(date)
}

private const val HEADER_DATE_FORMAT_PATTERN = "yyyy.MM"
fun String?.convertHeaderDateFormat(): String {
    this ?: return ""

    val milliseconds = this.toLong()
    val date = Date(milliseconds)
    val fromFormat = SimpleDateFormat(HEADER_DATE_FORMAT_PATTERN, Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }
    return fromFormat.format(date)
}

private const val RECORD_DATE_FORMAT_PATTERN = "MM.dd"
fun String?.convertRecordDateFormat(): String {
    this ?: return ""

    val milliseconds = this.toLong()
    val date = Date(milliseconds)
    val fromFormat = SimpleDateFormat(RECORD_DATE_FORMAT_PATTERN, Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }
    return fromFormat.format(date)
}

fun Float.km(context: Context) : String = context.getString(R.string.kilo_meter_unit, this)

fun Double.meter(context: Context) : String {
    return if(this < 1000) {
        context.getString(R.string.meter_unit, this)
    } else {
        context.getString(R.string.kilo_meter_unit, this/1000)
    }
}

fun Float.meter(context: Context) : String {
    return if(this < 1000) {
        context.getString(R.string.meter_unit, this)
    } else {
        context.getString(R.string.kilo_meter_unit, this/1000)
    }
}