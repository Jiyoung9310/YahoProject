package com.climbing.yaho.di.hilt

import com.climbing.yaho.local.YahoPreference
import com.climbing.yaho.local.YahoPreferenceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class PreferenceModule {
    @Binds
    abstract fun bindPreference(impl: YahoPreferenceImpl): YahoPreference
}