package com.enesduvan.kelepiravi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.enesduvan.kelepiravi.data.model.MarketItem

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
    val unlockedAchievements: String = "", // v4, comma-separated IDs
    val marketTrends: String = "",    // v5, JSON map of Category -> Multiplier
    val dailyRepairsUsed: Int = 0,    // v6 — Bugün kullanılan tamir sayısı
    val lastRepairDay: Int = 0,       // v6 — Son tamir günü (günlük sıfırlama için)
    val dailyRevenue: Double = 0.0,   // v7 — Günlük ciro (vergi için)
    val shopLevel: Int = 1,           // v8 — Dükkan seviyesi (Kapasite)
    val mechanicLevel: Int = 1        // v8 — Usta becerisi (Tamir)
)
