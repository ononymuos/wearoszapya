package com.bastyoliva.wearoszapya.di

import com.bastyoliva.wearable.navigation.Navigator
import com.bastyoliva.wearoszapya.navigation.NavigatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Binds
    @Singleton
    abstract fun bindNavigator(navigator: NavigatorImpl): Navigator
}