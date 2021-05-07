package com.android.yaho.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LiveClimbingDao {

    @Insert
    suspend fun insertRecord(recordEntity: RecordEntity)

    @Insert
    suspend fun insertSection(pathSectionEntity: PathSectionEntity)

    @Update
    suspend fun updateSection(pathSectionEntity: PathSectionEntity)

    @Insert
    suspend fun insertPoint(pointEntity: PointEntity)

    @Query("SELECT * FROM PointEntity WHERE parentSectionId == :parentSectionId")
    fun getPoints(parentSectionId: Long): Flow<List<PointEntity>>

    @Transaction
    @Query("SELECT * FROM RecordEntity")
    fun getRecordsWithSectionsAndPoints(): Flow<List<RecordWithSectionsAndPoints>>

    @Transaction
    @Query("SELECT * FROM PathSectionEntity")
    fun getSectionWithPoints(): Flow<List<SectionWithPoints>>

}