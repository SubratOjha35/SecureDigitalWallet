package com.faith.securedigitalwallet.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BankProfileDao {
    @Query("SELECT * FROM bank_profiles WHERE userOwnerId = :userId ORDER BY id DESC")
    fun getProfilesForUser(userId: Int): Flow<List<BankProfile>>

    @Insert
    suspend fun insertProfile(profile: BankProfile)

    @Update
    suspend fun updateProfile(profile: BankProfile)

    @Delete
    suspend fun deleteProfile(profile: BankProfile)
}
