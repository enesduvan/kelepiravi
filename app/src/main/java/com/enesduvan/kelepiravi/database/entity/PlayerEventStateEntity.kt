package com.enesduvan.kelepiravi.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_event_state")
data class PlayerEventStateEntity(

    @PrimaryKey
    val playerId: Int = 1,

    val eventFlags: String = "",

    val eventCooldowns: String = "{}"
)
