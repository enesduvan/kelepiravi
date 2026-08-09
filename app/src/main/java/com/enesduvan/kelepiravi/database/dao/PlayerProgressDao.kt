package com.enesduvan.kelepiravi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.enesduvan.kelepiravi.database.entity.PlayerProgressEntity
import kotlin.jvm.JvmSuppressWildcards
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface PlayerProgressDao {

    @Query("SELECT * FROM player_progress WHERE playerId = :playerId LIMIT 1")
    fun observePlayer(playerId: Int): Flow<PlayerProgressEntity?>

    @Query("SELECT * FROM player_progress WHERE playerId = :playerId LIMIT 1")
    suspend fun getPlayer(playerId: Int): PlayerProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlayerProgressEntity): Long

    @Update
    suspend fun update(entity: PlayerProgressEntity): Int

    @Query("UPDATE player_progress SET balance = :balance WHERE playerId = :playerId")
    suspend fun updateBalance(playerId: Int, balance: Long): Int

    @Query("UPDATE player_progress SET currentDay = :day WHERE playerId = :playerId")
    suspend fun updateDay(playerId: Int, day: Int): Int

    @Query("UPDATE player_progress SET xp = :xp, level = :level WHERE playerId = :playerId")
    suspend fun updateXpAndLevel(playerId: Int, xp: Int, level: Int): Int

    @Query("UPDATE player_progress SET shopLevel = :shopLevel WHERE playerId = :playerId")
    suspend fun updateShopLevel(playerId: Int, shopLevel: Int): Int

    @Query("UPDATE player_progress SET mechanicLevel = :mechanicLevel WHERE playerId = :playerId")
    suspend fun updateMechanicLevel(playerId: Int, mechanicLevel: Int): Int

    @Query("DELETE FROM player_progress WHERE playerId = :playerId")
    suspend fun deletePlayer(playerId: Int): Int
}
