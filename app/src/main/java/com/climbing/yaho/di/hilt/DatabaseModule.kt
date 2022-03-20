package com.climbing.yaho.di.hilt

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val YAHO_PREFERENCE = "yaho_preference"
@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): SharedPreferences {
        return appContext.getSharedPreferences(
            YAHO_PREFERENCE,
            Context.MODE_PRIVATE)
    }

}