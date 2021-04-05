package com.wasabicode.notificationstoxso.app

import android.app.Application
import com.wasabicode.notificationstoxso.app.di.provideHttpClient

class App : Application() {
    val configuration: Configuration = Configuration()
    val httpClient = provideHttpClient()
}