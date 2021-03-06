package com.climbing.yaho.local.cache

import android.location.Location
import android.util.Log
import com.climbing.yaho.convertFullFormatDate
import com.climbing.yaho.data.MountainData
import com.climbing.yaho.local.db.PathSectionEntity
import com.climbing.yaho.local.db.PointEntity
import com.climbing.yaho.local.db.RecordEntity

class LiveClimbingCache {
    private val _pointList = mutableListOf<PointEntity>()
    val pointList: List<PointEntity>
        get() = _pointList

    val latlngPaths: List<com.naver.maps.geometry.LatLng>
        get() = _pointList.map {
            com.naver.maps.geometry.LatLng(it.latitude, it.longitude)
        }

    private val _sectionList = mutableListOf<PathSectionEntity>()
    val sectionList: List<PathSectionEntity>
        get() = _sectionList

    private var _recordData : RecordEntity? = null
    private var sectionIndex: Int = 0

    fun initialize(mountain: MountainData, visitCount: Int) {
        if(_recordData == null) {
            _pointList.clear()
            _sectionList.clear()
            sectionIndex = 0
            _recordData = RecordEntity().apply {
                this.mountainId = mountain.id
                this.mountainName = mountain.name
                this.mountainVisitCount = visitCount
            }
            Log.d("LiveClimbingCache", "캐싱 initialize : $_recordData")
        }
    }

    fun clearCache() {
        _pointList.clear()
        _sectionList.clear()
        sectionIndex = 0
        _recordData = null
    }

    fun getRecord() = _recordData ?: RecordEntity()

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
        updateDistance()
        if(_pointList.count() == 1) updateSection()
        Log.d("LiveClimbingCache", "캐싱 : $_pointList")
    }

    private fun updateDistance() {
        _recordData?.let { it.totalDistance += _pointList.last().distance }
        Log.d("LiveClimbingCache", "캐싱 updateDistance : $_recordData")
    }

    fun updateSection() {
        if(_pointList.count() < 2) {
            _sectionList.add(PathSectionEntity())
            Log.d("LiveClimbingCache", "캐싱 섹션 : $_sectionList")
            return
        }

        val sectionPointList = _pointList.filter { it.parentSectionId == sectionIndex }

        if(sectionPointList.count() < 2) return

        _sectionList.add(
            PathSectionEntity(
                sectionId = sectionIndex++,
                runningTime = calculateRunningTime(sectionPointList),
                distance = sectionPointList.map { it.distance }.fold(0f) { total, num -> total + num },
                calories = 0f,
                restIndex = _pointList.lastIndex
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
            sections = mutableListOf<PathSectionEntity>().apply { addAll(_sectionList) }
            points = mutableListOf<PointEntity>().apply { addAll(_pointList) }
        }
        Log.d("LiveClimbingCache", "캐싱 완료 : $_recordData")
        return _recordData
    }

    private fun calculateRunningTime(list: List<PointEntity>) : Long = if(list.count() < 2) 0 else list.last().timestamp - list.first().timestamp
    private fun calculateClimbingTime(list: List<PathSectionEntity>) : Long = list.map { it.runningTime }.fold(0) { total, num -> total + num }
}