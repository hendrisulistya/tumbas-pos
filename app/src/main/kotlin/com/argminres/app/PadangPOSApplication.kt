package com.argminres.app

import android.app.Application
import com.argminres.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PadangPOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@PadangPOSApplication)
            modules(appModule)
        }
    }
}
