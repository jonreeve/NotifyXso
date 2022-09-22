package com.wasabicode.notifyxso.app.di

import com.wasabicode.notifyxso.app.features.main.AppCanSeeNotificationsUseCase
import com.wasabicode.notifyxso.app.features.main.CanSeeNotificationsUseCase
import com.wasabicode.notifyxso.app.shared.ConfigurationRepo
import com.wasabicode.notifyxso.app.shared.SharedPrefsConfigurationRepo
import com.wasabicode.notifyxso.app.shared.coroutines.AppDispatchers
import com.wasabicode.notifyxso.app.shared.coroutines.DefaultAppDispatchers
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
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
