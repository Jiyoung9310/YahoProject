package com.android.yaho.local.cache

import android.location.Location
import android.util.Log
import com.android.yaho.convertFullFormatDate
import com.android.yaho.data.ClimbingRecordData
import com.android.yaho.data.LiveClimbingData
import com.android.yaho.local.db.PathSectionEntity
import com.android.yaho.local.db.PointEntity
import com.android.yaho.local.db.RecordEntity
import com.naver.maps.geometry.LatLng

class LiveClimbingCache {
    private val _pointList = mutableListOf<PointEntity>()
    val pointList: List<PointEntity>
        get() = _pointList
    private val _latlngPaths = mutableListOf<LatLng>()
    val latlngPaths: List<LatLng>
        get() = _latlngPaths

    private val _sectionList = mutableListOf<PathSectionEntity>()
    val sectionList: List<PathSectionEntity>
        get() = _sectionList

    private var _recordData : RecordEntity? = null
    private var sectionIndex: Long = 0

    fun initialize(mountainId: Int) {
        _recordData = RecordEntity().apply { this.mountainId = mountainId }
        clearCache()
    }

    private fun clearCache() {
        _pointList.clear()
        _latlngPaths.clear()
        _sectionList.clear()
        _recordData = null
        sectionIndex = 0
    }

    fun getRecord() = _recordData

    fun getLastClimbingData() = _pointList.last()

    fun put(location: Location, distance: Float?) {
        _pointList.add(
            PointEntity(
                parentSectionId = sectionIndex,
                timestamp = location.time,
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                speed = location.speed,
                distance = distance ?: 0f
        ))
        _latlngPaths.add((LatLng(location.latitude, location.longitude)))
        updateDistance()
        Log.d("LiveClimbingCache", "캐싱 : $_pointList")
    }

    private fun updateDistance() {
        _recordData?.let { it.totalDistance += _pointList.last().distance }
    }

    fun updateSection() {
        if(_pointList.count() < 2) return

        val sectionPointList = _pointList.filter { it.parentSectionId == sectionIndex }

        if(sectionPointList.count() < 2) return

        _sectionList.add(
            PathSectionEntity(
                sectionId = sectionIndex++,
                runningTime = calculateRunningTime(sectionPointList),
                distance = _recordData?.totalDistance ?: 0f,
                calories = 0f
            )
        )
        Log.d("LiveClimbingCache", "캐싱 섹션 : $_sectionList")
    }

    fun done() : RecordEntity? {
        if(_pointList.count() < 2) return null
        updateSection()
        _recordData?.apply {
            climbingDate = convertFullFormatDate(_pointList.last().timestamp)
            allRunningTime = calculateRunningTime(_pointList)
            totalClimbingTime = calculateClimbingTime(_sectionList)
            maxSpeed = _pointList.maxOf { it.speed }
            averageSpeed = _pointList.map { it.speed }.average()
            startHeight = _pointList.first().altitude
            maxHeight = _pointList.map { it.altitude }.maxOf { it }

        }
        Log.d("LiveClimbingCache", "캐싱 완료 : $_recordData")
        return _recordData
    }

    private fun calculateRunningTime(list: List<PointEntity>) : Long = if(list.count() < 2) 0 else list.last().timestamp - list.first().timestamp
    private fun calculateClimbingTime(list: List<PathSectionEntity>) : Long = list.map { it.runningTime }.fold(0) { total, num -> total + num }
}