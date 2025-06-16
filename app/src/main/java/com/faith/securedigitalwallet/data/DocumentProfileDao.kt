package com.faith.securedigitalwallet.data

import androidx.room.*

@Dao
interface DocumentProfileDao {
    @Query("SELECT * FROM document_profile WHERE userId = :userId")
    suspend fun getByUserId(userId: Int): DocumentProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(doc: DocumentProfile)
}
