package com.android.yaho.data.cache

import android.location.Location
import android.util.Log
import com.android.yaho.data.ClimbingRecordData
import com.android.yaho.data.LiveClimbingData

class LiveClimbingCache {
    private val _data = mutableListOf<LiveClimbingData>()
    val data: List<LiveClimbingData>
        get() = _data
    private var _recordData : ClimbingRecordData? = null

    fun initialize(mountainId: Int) {
        _recordData = ClimbingRecordData().apply { this.mountainId = mountainId }
        _data.clear()
    }

    fun getRecord() = _recordData

    fun getLastClimbingData() = _data.last()

    fun put(location: Location, distance: Float?) {
        _data.add(
            LiveClimbingData(
                liveTime = location.time,
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                speed = location.speed,
                moveDistance = distance ?: 0f
        ))
        updateDistance()
        Log.d("LiveClimbingCache", "캐싱 : $_data")
    }

    private fun updateDistance() {
        _recordData?.let { it.totalDistance += _data.last().moveDistance }
    }

    fun done() {
        _recordData?.apply {
            highest = _data.map { it.altitude }.maxOf { it }
            runningTime = _data.last().liveTime - _data.first().liveTime
        }
        Log.d("LiveClimbingCache", "캐싱 완료 : $_recordData")
    }
}