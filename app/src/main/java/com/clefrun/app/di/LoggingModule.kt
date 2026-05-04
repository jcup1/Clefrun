package com.clefrun.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggingModule {
    @Binds
    abstract fun bindAppLogger(logger: AndroidAppLogger): AppLogger
}
