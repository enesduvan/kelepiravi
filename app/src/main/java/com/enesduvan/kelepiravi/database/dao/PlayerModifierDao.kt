package com.enesduvan.kelepiravi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.enesduvan.kelepiravi.database.entity.PlayerModifierEntity
import kotlin.jvm.JvmSuppressWildcards
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface PlayerModifierDao {

    @Query("SELECT * FROM player_modifiers WHERE playerId = :playerId AND expiresDay >= :currentDay")
    fun observeActiveModifiers(playerId: Int, currentDay: Int): Flow<List<PlayerModifierEntity>>

    @Query("SELECT * FROM player_modifiers WHERE playerId = :playerId")
    suspend fun getAllModifiers(playerId: Int): List<PlayerModifierEntity>

    @Query("""
        SELECT * FROM player_modifiers
        WHERE playerId = :playerId AND modifierId = :modifierId
        LIMIT 1
    """)
    suspend fun getModifier(playerId: Int, modifierId: String): PlayerModifierEntity?

    @Query("""
        SELECT value FROM player_modifiers
        WHERE playerId = :playerId AND modifierId = :modifierId AND expiresDay >= :currentDay
    """)
    suspend fun getActiveModifierValue(playerId: Int, modifierId: String, currentDay: Int): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlayerModifierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<PlayerModifierEntity>): List<Long>

    @Query("""
        UPDATE player_modifiers
        SET expiresDay = :newExpiresDay
        WHERE playerId = :playerId AND modifierId = :modifierId
    """)
    suspend fun extendExpiry(playerId: Int, modifierId: String, newExpiresDay: Int): Int

    @Query("DELETE FROM player_modifiers WHERE playerId = :playerId AND modifierId = :modifierId")
    suspend fun deleteModifier(playerId: Int, modifierId: String): Int

    @Query("DELETE FROM player_modifiers WHERE playerId = :playerId AND expiresDay < :currentDay")
    suspend fun deleteExpiredModifiers(playerId: Int, currentDay: Int): Int

    @Query("DELETE FROM player_modifiers WHERE playerId = :playerId")
    suspend fun deleteAllForPlayer(playerId: Int): Int
}
