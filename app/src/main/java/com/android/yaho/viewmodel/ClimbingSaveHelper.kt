package com.android.yaho.viewmodel


import android.location.Location
import android.util.Log
import com.android.yaho.local.db.PathSectionEntity
import com.android.yaho.local.db.PointEntity
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
/*

class ClimbingSaveHelper(private val roomdb: YahoRoomDatabase): CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private val TAG = this::class.java.simpleName

    private var sectionIndex: Long = 0
    private var mountainId: Int = 0

    private val _latlngPaths = mutableListOf<LatLng>()

    fun init(mountainId: Int) {
        this.mountainId = mountainId
    }

    fun savePoint(location: Location, distance: Float?) {
        launch(coroutineContext) {
            roomdb.climbingDao().insertPoint(
                PointEntity(
                    parentSectionId = sectionIndex,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    speed = location.speed,
                    timestamp = location.time,
                    distance = distance ?: 0f
                )
            )
            Log.i(TAG, "savePoint $location")
        }
        _latlngPaths.add((LatLng(location.latitude, location.longitude)))
    }

    fun getPointPath() = _latlngPaths

    fun updateSection() {
        launch(coroutineContext) {
            roomdb.climbingDao().getPoints(sectionIndex)
                .collect { list ->
                    var totalDistance = 0f

                    list.forEach {
                        totalDistance += it.distance
                    }

                    roomdb.climbingDao().insertSection(
                        PathSectionEntity(
                            runningTime = list.last().timestamp - list.first().timestamp,
                            distance = totalDistance,
                            calories = 0f
                        )
                    )
                }
            Log.i(TAG, "updateSection")
        }
    }

    fun saveRecord() {

    }
}*/
