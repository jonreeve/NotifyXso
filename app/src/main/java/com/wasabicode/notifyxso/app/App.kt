package com.wasabicode.notifyxso.app

import android.app.Application
import com.wasabicode.notifyxso.app.di.provideHttpClient

class App : Application() {
    lateinit var configurationRepo: ConfigurationRepo
    val httpClient = provideHttpClient()

    override fun onCreate() {
        super.onCreate()
        configurationRepo = SharedPrefsConfigurationRepo(this)
    }
}
