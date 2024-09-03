package com.simplifiedkiosk.database

import androidx.room.TypeConverter

class ImagesTypeConverted {
    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.joinToString(separator = ",")
    }

    @TypeConverter
    fun toList(string: String?): List<String>? {
        return string?.split(",")
    }
}