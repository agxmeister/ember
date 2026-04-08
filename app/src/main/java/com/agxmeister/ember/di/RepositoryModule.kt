package com.agxmeister.ember.di

import com.agxmeister.ember.data.repository.MeasurementRepositoryImpl
import com.agxmeister.ember.domain.repository.MeasurementRepository
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
    abstract fun bindMeasurementRepository(
        impl: MeasurementRepositoryImpl,
    ): MeasurementRepository
}
