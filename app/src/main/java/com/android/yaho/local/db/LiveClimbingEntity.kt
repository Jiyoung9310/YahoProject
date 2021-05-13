package com.android.yaho.local.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.naver.maps.geometry.LatLng


@Entity
data class RecordEntity(
    var mountainId: Int = 0,
    var mountainName: String = "",
    var climbingDate: String = "",
    var allRunningTime: Long = 0, // 전체 시간
    var totalClimbingTime: Long = 0, // 운동 시간
    var totalDistance: Float = 0f,
    var totalCalories: Float = 0f, // 몸무게 필요
    var maxSpeed: Float = 0f,
    var averageSpeed: Double = 0.0,
    var startHeight: Double = 0.0,
    var maxHeight: Double = 0.0,
    var sections: List<PathSectionEntity>? = null,
    var path: List<LatLng>? = null,
)

@Entity
data class PathSectionEntity(
    @PrimaryKey(autoGenerate = true) val sectionId: Long = 0,
    val runningTime: Long,
    var distance: Float = 0f,
    val calories: Float,
    val points: List<PointEntity>,
)

@Entity
data class PointEntity(
    val parentSectionId: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val timestamp: Long,
    val distance: Float,
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