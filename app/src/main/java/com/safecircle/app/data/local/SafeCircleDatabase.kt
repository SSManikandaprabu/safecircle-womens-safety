package com.safecircle.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.safecircle.app.data.local.dao.LocationDao
import com.safecircle.app.data.local.dao.UserDao
import com.safecircle.app.data.local.entity.LocationEntity
import com.safecircle.app.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, LocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SafeCircleDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao
}
