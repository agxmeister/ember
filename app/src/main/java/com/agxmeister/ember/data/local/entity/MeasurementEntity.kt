package com.agxmeister.ember.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "weight_kg") val weightKg: Double,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
)
