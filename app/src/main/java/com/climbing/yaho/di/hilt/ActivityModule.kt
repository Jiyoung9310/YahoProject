package com.climbing.yaho.di.hilt

import androidx.fragment.app.FragmentActivity
import com.climbing.yaho.di.ContextDelegate
import com.climbing.yaho.di.ContextDelegateImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
class ActivityModule {

    @Provides
    fun provideContextDelegate(
        activity: FragmentActivity
    ): ContextDelegate = ContextDelegateImpl(activity)

}