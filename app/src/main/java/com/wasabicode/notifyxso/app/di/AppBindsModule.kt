package com.wasabicode.notifyxso.app.di

import android.content.Context
import com.wasabicode.notifyxso.app.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModule {
    @Singleton @Binds
    abstract fun bindsConfigurationRepo(configurationRepo: SharedPrefsConfigurationRepo): ConfigurationRepo

    @Binds
    abstract fun bindsCanSeeNotificationsUseCase(appCanSeeNotificationsUseCase: AppCanSeeNotificationsUseCase): CanSeeNotificationsUseCase

    @Binds
    abstract fun bindsAppDispatchers(defaultAppDispatchers: DefaultAppDispatchers): AppDispatchers
}

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {
    @Singleton @Provides
    fun providesApp(@ApplicationContext context: Context) = context as App
}
