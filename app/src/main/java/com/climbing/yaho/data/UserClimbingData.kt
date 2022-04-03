package com.climbing.yaho.data


data class UserClimbingData(
    var allHeight: Double = 0.0,
    var allDistance: Float = 0f,
    var allTime: Long = 0,
    var totalCount: Int = 0,
)

data class UserData(
    val allHeight: Double = 0.0,
    val allDistance: Float = 0f,
    val allTime: Long = 0,
    val totalCount: Int = 0,
    val noAds: Boolean = false
)