package com.enesduvan.kelepiravi.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.enesduvan.kelepiravi.data.model.Listing

@Entity(tableName = "UserInventory")
data class UserInventoryEntity(

    @PrimaryKey
    val playerId: Int = 1,

    // Para
    val balance: Long = 0,

    // Oyun zamanı
    val currentDay: Int = 1,

    // XP / Level
    val xp: Int = 0,
    val level: Int = 1,

    // Dükkan
    val shopLevel: Int = 1,

    // Usta
    val mechanicLevel: Int = 1,

    // Günlük tamir sistemi
    val dailyRepairsUsed: Int = 0,
    val lastRepairDay: Int = 0,

    // Başarımlar / özel durumlar
    val unlockedAchievements: String = "",
    val hasBoughtScam: Boolean = false,
    val hasBoughtAbsurd: Boolean = false,
    val totalRepairs: Int = 0,
    val eventFlags: String = "",
    val eventCooldowns: String = "{}",
    val activeListings: List<Listing> = emptyList()
)
