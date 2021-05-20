package com.android.yaho

import android.app.Application
import com.android.yaho.di.appModule
import com.android.yaho.local.cache.MountainListCache
import com.facebook.stetho.Stetho
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
        if(BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)
    }
}