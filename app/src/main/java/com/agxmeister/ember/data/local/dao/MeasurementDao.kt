package com.agxmeister.ember.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.agxmeister.ember.data.local.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY timestamp ASC")
    fun getAll(): Flow<List<MeasurementEntity>>

    @Insert
    suspend fun insert(entity: MeasurementEntity)

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteById(id: Long)
}
