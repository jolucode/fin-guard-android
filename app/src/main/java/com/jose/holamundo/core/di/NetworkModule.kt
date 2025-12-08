package com.jose.holamundo.core.di

import com.jose.holamundo.core.config.AppConfig
import com.jose.holamundo.core.network.KtorClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return KtorClient.create(
            baseUrl = AppConfig.baseUrl,
            enableLogging = AppConfig.enableLogs
        )
    }
}

