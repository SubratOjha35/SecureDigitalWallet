// BankProfile.kt
package com.faith.securedigitalwallet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "bank_profiles",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BankProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountNumber: String,
    val type: String,
    val userId: String,
    val bank: String,
    val profilePassword: String,
    val mobileLoginPin: String,
    val upiPin: String,
    val atmPin: String,
    val password: String,
    val mobile: String,
    val userOwnerId: Int
)
