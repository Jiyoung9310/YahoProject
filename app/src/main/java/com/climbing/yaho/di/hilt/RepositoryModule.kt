package com.climbing.yaho.di.hilt

import com.climbing.yaho.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindLoginRepository(impl: LoginRepositoryImpl): LoginRepository

    @Binds
    abstract fun bindMountainRepository(impl: MountainRepositoryImpl): MountainRepository

    @Binds
    abstract fun bindUserDataRepository(impl: UserDataRepositoryImpl): UserDataRepository

    @Binds
    abstract fun bindClimbingRepository(impl: ClimbingRepositoryImpl): ClimbingRepository

}