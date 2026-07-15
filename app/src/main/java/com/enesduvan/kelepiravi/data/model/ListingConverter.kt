package com.enesduvan.kelepiravi.data.model

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ListingConverter {
    @TypeConverter
    fun fromList(value: List<Listing>): String = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String): List<Listing> = Json.decodeFromString(value)
}
