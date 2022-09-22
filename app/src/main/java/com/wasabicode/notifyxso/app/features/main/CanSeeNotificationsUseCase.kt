package com.wasabicode.notifyxso.app.features.main

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface CanSeeNotificationsUseCase {
    operator fun invoke(): Boolean
}

class AppCanSeeNotificationsUseCase @Inject constructor(@ApplicationContext private val appContext: Context) : CanSeeNotificationsUseCase {
    override fun invoke(): Boolean = NotificationManagerCompat.getEnabledListenerPackages(appContext).contains(appContext.packageName)
}


