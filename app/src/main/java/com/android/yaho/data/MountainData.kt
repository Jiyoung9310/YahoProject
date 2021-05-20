package com.android.yaho.data

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MountainData(
    val id: Int = 0,
    val name: String = "",
    val height: Float = 0f,
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val level: String = "",
) : Parcelable