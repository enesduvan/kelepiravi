package com.enesduvan.kelepiravi

import androidx.compose.ui.graphics.painter.Painter
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MarketItem(val condition: String ,
                      val sellerName: String ,
                      val itemName: String ,
                      val salesValue: String ,
                      val estimatedValue: String ,
                      val imageName : String)


class MarketItemConverter {
    @TypeConverter
    fun fromList(value: List<MarketItem>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toList(value: String): List<MarketItem> {
        return Json.decodeFromString(value)
    }
}