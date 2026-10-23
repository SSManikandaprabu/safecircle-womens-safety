package com.safecircle.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safecircle.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun observeUser(uid: String): Flow<UserEntity?>

    @Query("DELETE FROM users")
    suspend fun clear()
}
