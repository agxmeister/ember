package com.agxmeister.ember.di

import android.content.Context
import androidx.room.Room
import com.agxmeister.ember.data.local.EmberDatabase
import com.agxmeister.ember.data.local.dao.MeasurementDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EmberDatabase =
        Room.databaseBuilder(context, EmberDatabase::class.java, "ember.db").build()

    @Provides
    fun provideMeasurementDao(db: EmberDatabase): MeasurementDao = db.measurementDao()
}
