package com.enesduvan.kelepiravi.database.entity

import androidx.room.Entity
import androidx.room.Index

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
