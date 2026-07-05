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
    val currentDay: Int = 1           // Oyun günü — v2 migration ile eklendi
)
