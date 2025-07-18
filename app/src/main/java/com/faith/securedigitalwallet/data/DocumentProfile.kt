package com.faith.securedigitalwallet.data

import androidx.room.*

@Entity(
    tableName = "document_profile",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"], unique = true)]
)
data class DocumentProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val aadhaarPath: String? = null,
    val panCardPath: String? = null,
    val voterIdPath: String? = null,
    val drivingLicence: String? = null,
    @TypeConverters(Converters::class)
    val extraDocs: Map<String, String?>? = null
)
