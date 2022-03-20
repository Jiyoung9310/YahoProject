package com.climbing.yaho.di.hilt

import android.app.Application
import android.content.Context
import com.climbing.yaho.di.ContextDelegate
import com.climbing.yaho.di.ContextDelegateImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Provides
    fun provideContextDelegate(
        @ApplicationContext appContext: Context
    ): ContextDelegate = ContextDelegateImpl(appContext)

}