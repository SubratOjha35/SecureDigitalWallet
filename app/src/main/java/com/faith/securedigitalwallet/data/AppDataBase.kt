// AppDatabase.kt
package com.faith.securedigitalwallet.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [User::class, BankProfile::class, UserDocument::class, UserDocumentFiles::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bankProfileDao(): BankProfileDao
    abstract fun userDocDao(): UserDocDao
    abstract fun userDocFilesDao(): UserDocFilesDao

}
