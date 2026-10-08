package com.bastyoliva.wearoszapya.di

import com.bastyoliva.wearable.data.repository.ClipboardRepository
import com.bastyoliva.wearable.data.repository.FileContentRepository
import com.bastyoliva.wearable.data.repository.FileManagerRepository
import com.bastyoliva.wearable.data.repository.PinnedRepository
import com.bastyoliva.wearable.data.repository.RemoteInteractionHandler
import com.bastyoliva.wearable.design.theme.ThemeManager
import com.bastyoliva.wearoszapya.data.ClipboardRepositoryImpl
import com.bastyoliva.wearoszapya.data.FileContentRepositoryImpl
import com.bastyoliva.wearoszapya.data.FileManagerRepositoryImpl
import com.bastyoliva.wearoszapya.data.PinnedRepositoryImpl
import com.bastyoliva.wearoszapya.data.RemoteInteractionHandlerImpl
import com.bastyoliva.wearoszapya.data.ThemeManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindFileManagerRepository(
        impl: FileManagerRepositoryImpl
    ): FileManagerRepository

    @Binds
    @Singleton
    fun bindClipboardRepository(
        impl: ClipboardRepositoryImpl
    ): ClipboardRepository

    @Binds
    @Singleton
    fun bindFileContentRepository(
        impl: FileContentRepositoryImpl
    ): FileContentRepository

    @Binds
    fun bindRemoteRepository(
        impl: RemoteInteractionHandlerImpl
    ): RemoteInteractionHandler

    @Binds
    @Singleton
    fun bindPinnedRepository(
        impl: PinnedRepositoryImpl
    ): PinnedRepository

    @Binds
    @Singleton
    fun bindThemeManager(
        impl: ThemeManagerImpl
    ): ThemeManager
}