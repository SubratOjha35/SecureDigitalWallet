package com.faith.securedigitalwallet.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.ABORT) // Prevent replacing existing user
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE name = :name)")
    suspend fun userExists(name: String): Boolean
}
