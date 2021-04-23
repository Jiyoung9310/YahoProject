package com.android.yaho.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MountainData(
    val id: Int = 0,
    val name: String = "",
    val height: Int = 0,
    val address: String = "",
    val latitude: Float = 0f,
    val longitude: Float = 0f,
    val level: String = "",
) : Parcelable