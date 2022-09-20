package com.wasabicode.notifyxso.app.di

import com.wasabicode.notifyxso.app.ConfigurationRepo
import com.wasabicode.notifyxso.app.SharedPrefsConfigurationRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Singleton @Binds
    abstract fun providesConfigurationRepo(configurationRepo: SharedPrefsConfigurationRepo): ConfigurationRepo
}
