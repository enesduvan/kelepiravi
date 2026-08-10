package com.enesduvan.kelepiravi.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
