package com.enesduvan.kelepiravi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.enesduvan.kelepiravi.database.entity.PlayerEventStateEntity
import kotlin.jvm.JvmSuppressWildcards

@Dao
@JvmSuppressWildcards
interface PlayerEventStateDao {

    @Query("SELECT * FROM player_event_state WHERE playerId = :playerId LIMIT 1")
    suspend fun getEventState(playerId: Int): PlayerEventStateEntity?

    @Query("SELECT eventFlags FROM player_event_state WHERE playerId = :playerId")
    suspend fun getEventFlags(playerId: Int): String?

    @Query("SELECT eventCooldowns FROM player_event_state WHERE playerId = :playerId")
    suspend fun getEventCooldowns(playerId: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlayerEventStateEntity): Long

    @Query("UPDATE player_event_state SET eventFlags = :flags WHERE playerId = :playerId")
    suspend fun updateEventFlags(playerId: Int, flags: String): Int

    @Query("UPDATE player_event_state SET eventCooldowns = :cooldowns WHERE playerId = :playerId")
    suspend fun updateEventCooldowns(playerId: Int, cooldowns: String): Int

    @Query("""
        UPDATE player_event_state
        SET eventFlags = :flags, eventCooldowns = :cooldowns
        WHERE playerId = :playerId
    """)
    suspend fun updateFlagsAndCooldowns(playerId: Int, flags: String, cooldowns: String): Int

    @Query("DELETE FROM player_event_state WHERE playerId = :playerId")
    suspend fun deleteEventState(playerId: Int): Int
}
