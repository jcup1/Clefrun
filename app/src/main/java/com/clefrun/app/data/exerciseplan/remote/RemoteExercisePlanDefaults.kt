package com.clefrun.app.data.exerciseplan.remote

import java.util.concurrent.TimeUnit
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

internal val RemoteJsonMediaType = "application/json; charset=utf-8".toMediaType()

internal fun defaultJson(): Json {
    return Json {
        ignoreUnknownKeys = true
    }
}

internal fun defaultOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
}

internal fun defaultRemoteExercisePlanApi(
    config: RemoteExercisePlanConfig,
    client: OkHttpClient = defaultOkHttpClient(),
    json: Json = defaultJson(),
): RemoteExercisePlanApi {
    return defaultRetrofit(config, client, json)
        .create(RemoteExercisePlanApi::class.java)
}

internal fun defaultRemoteExercisePlanProvider(
    config: RemoteExercisePlanConfig,
    client: OkHttpClient = defaultOkHttpClient(),
    json: Json = defaultJson(),
): RemoteExercisePlanProvider {
    return RemoteExercisePlanProvider(
        config = config,
        api = defaultRemoteExercisePlanApi(config, client, json),
    )
}

@OptIn(ExperimentalSerializationApi::class)
internal fun defaultRetrofit(
    config: RemoteExercisePlanConfig,
    client: OkHttpClient,
    json: Json,
): Retrofit {
    return Retrofit.Builder()
        .baseUrl(config.baseUrl.normalizedRetrofitBaseUrl())
        .client(client)
        .addConverterFactory(json.asConverterFactory(RemoteJsonMediaType))
        .build()
}

private fun String.normalizedRetrofitBaseUrl(): String {
    return trimEnd('/') + "/"
}
