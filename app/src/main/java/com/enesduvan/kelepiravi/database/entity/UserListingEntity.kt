package com.enesduvan.kelepiravi.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
