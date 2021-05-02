package com.android.yaho.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [RecordEntity::class,
    PathSectionEntity::class,
    PointEntity::class], version = 1)
abstract class YahoRoomDatabase : RoomDatabase() {
    abstract fun climbingDao(): LiveClimbingDao

    companion object {
        private val DB_NAME = "room-db"
        private var instance: YahoRoomDatabase? = null

        fun getInstance(context: Context): YahoRoomDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context)
            }
        }

        private fun buildDatabase(context: Context): YahoRoomDatabase {
            return Room.databaseBuilder(context.applicationContext, YahoRoomDatabase::class.java, DB_NAME)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }
                }).build()
        }
    }

}