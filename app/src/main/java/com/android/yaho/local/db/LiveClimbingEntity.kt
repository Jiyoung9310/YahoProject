package com.android.yaho.local.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation


@Entity
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val recordId: Long = 0,
    val allRunningTime: Long,
    val totalClimbingTime: Long,
    val totalDistance: Float,
    val totalCalories: Float,
    val maxSpeed: Float,
    val averageSpeed: Float,
    val startHeight: Float,
    val maxHeight: Float,
)

@Entity
data class PathSectionEntity(
    @PrimaryKey(autoGenerate = true) val sectionId: Long = 0,
    val parentRecordId: Long = 0,
    val runningTime: Long,
    val distance: Float,
    val calories: Float,
)

@Entity
data class PointEntity(
    @PrimaryKey(autoGenerate = true) val pointId: Long = 0,
    val parentSectionId: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val timestamp: Long,
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