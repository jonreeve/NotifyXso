package com.wasabicode.notifyxso.app

import android.app.Application
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.SharedPrefsConfiguration
import com.wasabicode.notifyxso.app.di.provideHttpClient

class App : Application() {
    lateinit var configuration: Configuration
    val httpClient = provideHttpClient()

    override fun onCreate() {
        super.onCreate()
        configuration = SharedPrefsConfiguration(this)
    }
}