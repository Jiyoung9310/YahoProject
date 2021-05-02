package com.android.yaho.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LiveClimbingDao {

    @Insert
    fun insertRecord(recordEntity: RecordEntity)

    @Insert
    fun insertSection(pathSectionEntity: PathSectionEntity)

    @Insert
    fun insertPoint(pointEntity: PointEntity)

    @Transaction
    @Query("SELECT * FROM RecordEntity")
    fun getRecordsWithSectionsAndPoints(): Flow<List<RecordWithSectionsAndPoints>>

    @Transaction
    @Query("SELECT * FROM PathSectionEntity")
    fun getSectionWithPoints(): Flow<List<SectionWithPoints>>

    @Delete
    fun deleteRecord(recordId: Long)
}