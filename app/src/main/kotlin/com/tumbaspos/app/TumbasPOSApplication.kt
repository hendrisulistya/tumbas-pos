package com.tumbaspos.app

import android.app.Application
import com.tumbaspos.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TumbasPOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@TumbasPOSApplication)
            modules(appModule)
        }
    }
}
