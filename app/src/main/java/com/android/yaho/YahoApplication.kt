package com.android.yaho

import android.app.Application
import com.android.yaho.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin


class YahoApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // Start Koin
        startKoin{
            androidLogger()
            androidContext(this@YahoApplication)
            modules(appModule)
        }


    }
}