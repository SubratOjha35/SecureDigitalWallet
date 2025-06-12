package com.faith.securedigitalwallet.data

import androidx.room.*

@Dao
interface UserDocFilesDao {
    @Query("SELECT * FROM user_documents WHERE userId = :userId")
    suspend fun getByUserId(userId: Int): UserDocumentFiles?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(doc: UserDocumentFiles)
}
