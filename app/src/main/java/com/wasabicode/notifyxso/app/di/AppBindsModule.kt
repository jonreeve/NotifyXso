package com.wasabicode.notifyxso.app.di

import android.content.Context
import com.wasabicode.notifyxso.app.App
import com.wasabicode.notifyxso.app.ConfigurationRepo
import com.wasabicode.notifyxso.app.SharedPrefsConfigurationRepo
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
    abstract fun providesConfigurationRepo(configurationRepo: SharedPrefsConfigurationRepo): ConfigurationRepo
}

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {
    @Singleton @Provides
    fun providesApp(@ApplicationContext context: Context) = context as App
}
