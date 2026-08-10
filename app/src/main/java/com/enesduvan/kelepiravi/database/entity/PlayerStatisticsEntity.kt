package com.enesduvan.kelepiravi.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
