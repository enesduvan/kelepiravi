package com.enesduvan.kelepiravi.data.model

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MarketItemConverter {
    @TypeConverter
    fun fromList(value: List<MarketItem>): String = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String): List<MarketItem> = Json.decodeFromString(value)
}
