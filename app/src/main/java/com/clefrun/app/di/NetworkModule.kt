package com.clefrun.app.di

import com.clefrun.app.data.exerciseplan.remote.RemoteExercisePlanApi
import com.clefrun.app.data.exerciseplan.remote.RemoteExercisePlanConfig
import com.clefrun.app.data.exerciseplan.remote.defaultJson
import com.clefrun.app.data.exerciseplan.remote.defaultOkHttpClient
import com.clefrun.app.data.exerciseplan.remote.defaultRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json {
        return defaultJson()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return defaultOkHttpClient()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        config: RemoteExercisePlanConfig,
        client: OkHttpClient,
        json: Json,
    ): Retrofit {
        return defaultRetrofit(config, client, json)
    }

    @Provides
    @Singleton
    fun provideRemoteExercisePlanApi(retrofit: Retrofit): RemoteExercisePlanApi {
        return retrofit.create(RemoteExercisePlanApi::class.java)
    }
}
