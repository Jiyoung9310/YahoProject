package com.android.yaho.data


data class UserClimbingData(
    var allHeight: Double = 0.0,
    var allDistance: Float = 0f,
    var allTime: Long = 0,
    var totalCount: Int = 0,
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