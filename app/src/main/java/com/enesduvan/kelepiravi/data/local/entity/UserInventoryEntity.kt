package com.enesduvan.kelepiravi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.enesduvan.kelepiravi.data.model.Listing
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
    val mechanicLevel: Int = 1,       // v8 — Usta becerisi (Tamir)
    val eventFlags: String = "",      // v9 — Event Engine flag'leri (Örn: "MET_58_PLAKA,HELPED_OLD_MAN")
    val eventCooldowns: String = "",  // v9 — Event Engine bekleme süreleri (JSON map: EventId -> UnlocksDay)
    val activeListings: List<Listing> = emptyList(), // v10 - İlan Sistemi
    val npcRelationships: String = "{}", // v11 - NPC İlişki Skorları (JSON map: String -> Int)
    val highestProfit: Double = 0.0, // v12 - En yüksek kâr
    val rareItemsFound: Int = 0, // v12 - Bulunan nadir eşya
    val totalRepairs: Int = 0, // v13 - Toplam Tamir Sayısı
    val hasBoughtScam: Boolean = false, // v13 - Dolandırılma Durumu
    val hasBoughtAbsurd: Boolean = false, // v13 - Absürt eşya (NASA vb.) alım durumu
    val activeModifiers: String = "{}" // v14 - Aktif Modifierlar (JSON Map: String -> Int)
)
