package com.enesduvan.kelepiravi.database.entity

import androidx.room.Entity

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
