package com.bastyoliva.wearoszapya.di

import android.content.Context
import com.bastyoliva.wearable.data.repository.FileInteractionHandler
import com.bastyoliva.wearoszapya.data.FileInteractionHandlerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FileInteractionModule {

    @Provides
    @Singleton
    fun provideFileInteractionHandler(
        @ApplicationContext context: Context
    ): FileInteractionHandler = FileInteractionHandlerImpl(context)
}