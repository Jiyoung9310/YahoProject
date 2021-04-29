package com.android.yaho.data


data class UserClimbingData(
    val allHeight: Float = 0f,
    val allDistance: Float = 0f,
    val allTime: Int = 0,
    val totalCount: Int = 0,
)

data class ClimbingRecordData(
    var mountainId: Int = 0,
    var highest: Double = 0.0,
    var totalDistance: Float = 0f,
    var runningTime: Long = 0,
)

data class LiveClimbingData (
    val liveTime : Long,
    val latitude : Double,
    val longitude : Double,
    val altitude : Double,
    val speed : Float,
    val moveDistance: Float,
)