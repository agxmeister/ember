package com.agxmeister.ember.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.agxmeister.ember.data.local.dao.MeasurementDao
import com.agxmeister.ember.data.local.entity.MeasurementEntity

@Database(entities = [MeasurementEntity::class], version = 1)
abstract class EmberDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao
}
