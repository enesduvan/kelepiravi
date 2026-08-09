package com.enesduvan.kelepiravi.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.enesduvan.kelepiravi.data.model.Listing
import com.enesduvan.kelepiravi.data.model.MarketItem

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
@Entity(tableName = "player_progress")
data class PlayerProgressEntity(
    @PrimaryKey
    val playerId: Int = 1,

    val balance: Long = 0,
    val currentDay: Int = 1,

    val xp: Int = 0,
    val level: Int = 1,

    val shopLevel: Int = 1,
    val mechanicLevel: Int = 1
)

@Entity(
    tableName = "user_inventory_items",
    foreignKeys = [
        ForeignKey(
            entity = PlayerProgressEntity::class,
            parentColumns = ["playerId"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playerId")]
)
data class UserInventoryItemEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val playerId: Int = 1,

    val itemId: String,

    val quantity: Int = 1,

    val purchasePrice: Double,

    val condition: Int,

    /** Full item snapshot. Keeping this avoids losing scam/condition metadata. */
    val itemJson: String = ""
)
@Entity(
    tableName = "user_listings",
    indices = [Index("playerId")]
)
data class UserListingEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val playerId: Int = 1,

    val itemId: Long,

    val askingPrice: Double,

    val listedDay: Int,

    val isActive: Boolean = true,

    /** Full listing snapshot for views, offers and item metadata. */
    val listingJson: String = ""
)

@Entity(tableName = "player_statistics")
data class PlayerStatisticsEntity(

    @PrimaryKey
    val playerId: Int = 1,

    val totalProfit: Double = 0.0,

    val itemsBought: Int = 0,

    val itemsSold: Int = 0,

    val highestProfit: Double = 0.0,

    val rareItemsFound: Int = 0,

    val totalRepairs: Int = 0,

    val successfulBargains: Int = 0,

    val totalBargains: Int = 0,

    val dailyRevenue: Double = 0.0,

    val soldCategories: String = "{}"
)
@Entity(tableName = "player_event_state")
data class PlayerEventStateEntity(

    @PrimaryKey
    val playerId: Int = 1,

    val eventFlags: String = "",

    val eventCooldowns: String = "{}"
)

@Entity(
    tableName = "player_npc_relationships",
    primaryKeys = ["playerId", "npcId"],
    indices = [Index("npcId")]
)
data class PlayerNpcRelationshipEntity(

    val playerId: Int = 1,

    val npcId: String,

    val relationshipScore: Int = 0
)

@Entity(
    tableName = "player_modifiers",
    primaryKeys = ["playerId", "modifierId"]
)
data class PlayerModifierEntity(

    val playerId: Int = 1,

    val modifierId: String,

    val value: Int,

    val expiresDay: Int
)
