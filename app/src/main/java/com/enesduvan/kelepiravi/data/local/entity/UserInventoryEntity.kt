package com.enesduvan.kelepiravi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.enesduvan.kelepiravi.data.model.MarketItem

const val DEFAULT_USER_ID = 1
const val INITIAL_BALANCE = "25000.0"

@Entity(tableName = "UserInventory")
data class UserInventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val balance: String,
    val inventory: List<MarketItem>,
    val currentDay: Int = 1,          // v2 migration
    val xp: Int = 0,                  // v3
    val level: Int = 1,               // v3
    val totalProfit: Double = 0.0,    // v3
    val itemsBought: Int = 0,         // v3
    val itemsSold: Int = 0,           // v3
    val unlockedAchievements: String = "" // v4, comma-separated IDs
)
