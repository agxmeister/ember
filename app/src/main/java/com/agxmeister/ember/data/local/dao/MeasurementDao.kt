package com.agxmeister.ember.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.agxmeister.ember.data.local.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY timestamp ASC")
    fun getAll(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE timestamp >= :fromMs AND timestamp < :toMs ORDER BY timestamp ASC")
    fun getForDateRange(fromMs: Long, toMs: Long): Flow<List<MeasurementEntity>>

    @Insert
    suspend fun insert(entity: MeasurementEntity)

    @Update
    suspend fun update(entity: MeasurementEntity)

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM measurements WHERE timestamp >= :fromMs ORDER BY timestamp ASC")
    suspend fun getSince(fromMs: Long): List<MeasurementEntity>
}
