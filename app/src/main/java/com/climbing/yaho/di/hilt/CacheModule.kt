package com.climbing.yaho.di.hilt

import com.climbing.yaho.local.cache.LiveClimbingCache
import com.climbing.yaho.local.cache.MountainListCache
import com.climbing.yaho.repository.MountainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    @Singleton
    @Provides
    fun provideMountainListCache(repo : MountainRepository): MountainListCache
        = MountainListCache(repo)

    @Singleton
    @Provides
    fun provideLiveClimbingCache(): LiveClimbingCache = LiveClimbingCache()
}