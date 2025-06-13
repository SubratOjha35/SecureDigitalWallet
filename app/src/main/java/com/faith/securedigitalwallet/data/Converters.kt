package com.faith.securedigitalwallet.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromMap(value: Map<String, String?>?): String {
        return gson.toJson(value ?: emptyMap<String, String?>())
    }

    @TypeConverter
    fun toMap(value: String?): Map<String, String?> {
        if (value.isNullOrBlank() || value == "null") return emptyMap()
        val mapType: Type = object : TypeToken<Map<String, String?>>() {}.type
        return gson.fromJson(value, mapType)
    }
}
