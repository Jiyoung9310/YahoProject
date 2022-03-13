package com.climbing.yaho

import android.app.Application
import com.climbing.yaho.BuildConfig
import com.climbing.yaho.di.appModule
import com.climbing.yaho.local.cache.MountainListCache
import com.facebook.stetho.Stetho
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.GlobalContext.startKoin
import javax.inject.Inject

@HiltAndroidApp
class YahoApplication: Application() {

    @Inject
    lateinit var mountainListCache: MountainListCache

    @InternalCoroutinesApi
    override fun onCreate() {
        super.onCreate()
        // Start Koin
        /*startKoin{
            androidLogger()
            androidContext(this@YahoApplication)
            modules(appModule)
        }*/
        mountainListCache.initialize()
        if(BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)
    }
}