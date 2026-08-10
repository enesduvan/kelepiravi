package com.enesduvan.kelepiravi.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
