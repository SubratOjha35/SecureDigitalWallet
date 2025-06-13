package com.faith.securedigitalwallet.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDocDao {

    @Query("SELECT * FROM usersdoc")
    fun getAllUsers(): Flow<List<UserDocument>>

    @Insert(onConflict = OnConflictStrategy.ABORT) // Prevent replacing existing user
    suspend fun insertUser(user: UserDocument)

    @Delete
    suspend fun deleteUser(user: UserDocument)

    @Query("SELECT EXISTS(SELECT 1 FROM usersdoc WHERE name = :name)")
    suspend fun userExists(name: String): Boolean
}
