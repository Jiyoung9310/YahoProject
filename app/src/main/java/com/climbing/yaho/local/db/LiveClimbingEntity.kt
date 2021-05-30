package com.android.yaho.local.db

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class LatLng(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
) : Parcelable

@Entity
data class RecordEntity(
    var recordId : String = "",
    var mountainId: Int = 0,
    var mountainName: String = "",
    var mountainVisitCount: Int = 0,
    var climbingDate: String = "",
    var allRunningTime: Long = 0, // 전체 시간
    var totalClimbingTime: Long = 0, // 운동 시간
    var totalDistance: Float = 0f, // meter
    var totalCalories: Float = 0f, // 몸무게 필요
    var maxSpeed: Float = 0f,
    var averageSpeed: Double = 0.0,
    var startHeight: Double = 0.0,
    var maxHeight: Double = 0.0,
    var sections: List<PathSectionEntity>? = null,
    var points: List<PointEntity>? = null,
)

@Entity
data class PathSectionEntity(
    @PrimaryKey(autoGenerate = true) val sectionId: Int = 0,
    val runningTime: Long = 0,
    var distance: Float = 0f, // meter
    val calories: Float = 0f,
    val restIndex: Int = 0
)

@Entity
data class PointEntity(
    val parentSectionId: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val speed: Float = 0f,
    val timestamp: Long = 0,
    val distance: Float = 0f, // meter
)

data class RecordWithSectionsAndPoints(
    @Embedded val record: RecordEntity,
    @Relation(
        entity = PathSectionEntity::class,
        parentColumn = "recordId",
        entityColumn = "parentRecordId"
    )
    val points: List<SectionWithPoints>
)

data class SectionWithPoints(
    @Embedded val section: PathSectionEntity,
    @Relation(
        parentColumn = "sectionId",
        entityColumn = "parentSectionId"
    )
    val points: List<PointEntity>
)