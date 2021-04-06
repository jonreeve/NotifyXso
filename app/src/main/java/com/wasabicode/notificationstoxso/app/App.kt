package com.wasabicode.notificationstoxso.app

import android.app.Application
import com.wasabicode.notificationstoxso.app.config.Configuration
import com.wasabicode.notificationstoxso.app.config.SharedPrefsConfiguration
import com.wasabicode.notificationstoxso.app.di.provideHttpClient

class App : Application() {
    lateinit var configuration: Configuration
    val httpClient = provideHttpClient()

    override fun onCreate() {
        super.onCreate()
        configuration = SharedPrefsConfiguration(this)
    }
}