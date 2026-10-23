package com.safecircle.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.safecircle.app.data.local.entity.LocationEntity

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: LocationEntity): Long

    @Query("SELECT * FROM location_queue WHERE synced = 0 ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getUnsynced(limit: Int = 100): List<LocationEntity>

    @Query("UPDATE location_queue SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM location_queue WHERE synced = 1 AND timestamp < :before")
    suspend fun purgeSyncedBefore(before: Long)

    @Query("SELECT COUNT(*) FROM location_queue WHERE synced = 0")
    suspend fun unsyncedCount(): Int
}
