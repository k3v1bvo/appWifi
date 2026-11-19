package com.smartwifi.switcher.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GeoZoneDao {
    @Query("SELECT * FROM geo_zones ORDER BY id DESC")
    fun getAllZones(): Flow<List<GeoZone>>

    @Query("SELECT * FROM geo_zones")
    suspend fun getAllZonesList(): List<GeoZone>

    @Query("SELECT * FROM geo_zones WHERE isEnabled = 1")
    suspend fun getActiveZones(): List<GeoZone>

    @Query("SELECT * FROM geo_zones WHERE id = :id LIMIT 1")
    suspend fun getZoneById(id: Long): GeoZone?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZone(zone: GeoZone): Long

    @Update
    suspend fun updateZone(zone: GeoZone)

    @Delete
    suspend fun deleteZone(zone: GeoZone)

    @Query("UPDATE geo_zones SET lastTriggeredAt = :time WHERE id = :id")
    suspend fun updateLastTriggered(id: Long, time: Long)
}
