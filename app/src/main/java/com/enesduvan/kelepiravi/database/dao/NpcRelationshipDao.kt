package com.enesduvan.kelepiravi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.enesduvan.kelepiravi.database.entity.PlayerNpcRelationshipEntity
import kotlin.jvm.JvmSuppressWildcards
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface NpcRelationshipDao {

    @Query("SELECT * FROM player_npc_relationships WHERE playerId = :playerId")
    fun observeRelationships(playerId: Int): Flow<List<PlayerNpcRelationshipEntity>>

    @Query("SELECT * FROM player_npc_relationships WHERE playerId = :playerId")
    suspend fun getAllRelationships(playerId: Int): List<PlayerNpcRelationshipEntity>

    @Query("""
        SELECT relationshipScore
        FROM player_npc_relationships
        WHERE playerId = :playerId AND npcId = :npcId
    """)
    suspend fun getScore(playerId: Int, npcId: String): Int?

    @Query("""
        SELECT * FROM player_npc_relationships
        WHERE playerId = :playerId AND npcId = :npcId
        LIMIT 1
    """)
    suspend fun getRelationship(playerId: Int, npcId: String): PlayerNpcRelationshipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlayerNpcRelationshipEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<PlayerNpcRelationshipEntity>): List<Long>

    @Query("""
        UPDATE player_npc_relationships
        SET relationshipScore = 0
        WHERE playerId = :playerId AND npcId = :npcId
    """)
    suspend fun resetScore(playerId: Int, npcId: String): Int

    @Query("DELETE FROM player_npc_relationships WHERE playerId = :playerId AND npcId = :npcId")
    suspend fun deleteRelationship(playerId: Int, npcId: String): Int

    @Query("DELETE FROM player_npc_relationships WHERE playerId = :playerId")
    suspend fun deleteAllForPlayer(playerId: Int): Int
}
