package com.android.yaho.data


data class UserClimbingData(
    val allHeight: Float = 0f,
    val allDistance: Float = 0f,
    val allTime: Int = 0,
    val records: List<ClimbingRecordData> = emptyList()
)

data class ClimbingRecordData(
    val mountainId: Int = 0,
    val height: Float = 0f,
    val distance: Float = 0f,
    val runningTime: Int = 0,
)
