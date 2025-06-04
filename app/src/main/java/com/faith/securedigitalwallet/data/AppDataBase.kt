// AppDatabase.kt
package com.faith.securedigitalwallet.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [User::class, BankProfile::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bankProfileDao(): BankProfileDao
}
