package com.jose.holamundo.core.di

import com.jose.holamundo.data.remote.api.NotificationApi
import com.jose.holamundo.data.remote.api.NotificationApiImpl
import com.jose.holamundo.data.repository.NotificationRepository
import com.jose.holamundo.data.repository.NotificationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNotificationApi(
        impl: NotificationApiImpl
    ): NotificationApi

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository
}
