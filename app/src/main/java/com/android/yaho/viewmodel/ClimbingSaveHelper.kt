package com.android.yaho.viewmodel


import android.location.Location
import com.android.yaho.local.db.PathSectionEntity
import com.android.yaho.local.db.PointEntity
import com.android.yaho.local.db.YahoRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ClimbingSaveHelper(private val roomdb: YahoRoomDatabase): CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private var sectionIndex: Long = 0
    private var mountainId: Int = 0

    fun init(mountainId: Int) {
        this.mountainId = mountainId
    }

    fun savePoint(location: Location, distance: Float?) {
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
    }

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
                            sectionId = sectionIndex++,
                            parentRecordId = 0,
                            runningTime = list.last().timestamp - list.first().timestamp,
                            distance = totalDistance,
                            calories = 0f
                        )
                    )
                }
        }
    }

    fun saveRecord() {

    }
}