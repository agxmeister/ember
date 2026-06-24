package com.agxmeister.ember.di

import com.agxmeister.ember.data.analytics.AptabaseAnalyticsTracker
import com.agxmeister.ember.domain.analytics.AnalyticsTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(
        impl: AptabaseAnalyticsTracker,
    ): AnalyticsTracker
}
