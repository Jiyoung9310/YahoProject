package com.android.yaho

import android.app.Application
import com.android.yaho.data.cache.MountainListCache
import com.android.yaho.di.appModule
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.GlobalContext.startKoin


class YahoApplication: Application(), KoinComponent {

    @InternalCoroutinesApi
    override fun onCreate() {
        super.onCreate()
        // Start Koin
        startKoin{
            androidLogger()
            androidContext(this@YahoApplication)
            modules(appModule)
        }
        get<MountainListCache>().initialize()
    }
}