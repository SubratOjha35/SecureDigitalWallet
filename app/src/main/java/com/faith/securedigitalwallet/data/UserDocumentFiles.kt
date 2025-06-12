package com.faith.securedigitalwallet.data

import androidx.room.*

@Entity(
    tableName = "user_documents",
    foreignKeys = [ForeignKey(
        entity = UserDocument::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserDocumentFiles(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val aadhaarPath: String? = null,
    val panCardPath: String? = null,
    val voterIdPath: String? = null,
    val drivingLicence: String? = null,
    @TypeConverters(Converters::class)
    val extraDocs: Map<String, String?>? = null
)
