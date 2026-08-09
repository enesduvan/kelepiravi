package com.enesduvan.kelepiravi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.enesduvan.kelepiravi.database.entity.PlayerStatisticsEntity
import kotlin.jvm.JvmSuppressWildcards
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface PlayerStatisticsDao {

    @Query("SELECT * FROM player_statistics WHERE playerId = :playerId LIMIT 1")
    fun observeStatistics(playerId: Int): Flow<PlayerStatisticsEntity?>

    @Query("SELECT * FROM player_statistics WHERE playerId = :playerId LIMIT 1")
    suspend fun getStatistics(playerId: Int): PlayerStatisticsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlayerStatisticsEntity): Long

    @Update
    suspend fun update(entity: PlayerStatisticsEntity): Int

    @Query("UPDATE player_statistics SET itemsBought = itemsBought + 1 WHERE playerId = :playerId")
    suspend fun incrementItemsBought(playerId: Int): Int

    @Query("""
        UPDATE player_statistics
        SET itemsSold = itemsSold + 1,
            totalProfit = totalProfit + :profit,
            dailyRevenue = dailyRevenue + :salePrice
        WHERE playerId = :playerId
    """)
    suspend fun recordSale(playerId: Int, profit: Double, salePrice: Double): Int

    @Query("UPDATE player_statistics SET totalRepairs = totalRepairs + 1 WHERE playerId = :playerId")
    suspend fun incrementRepairs(playerId: Int): Int

    @Query("""
        UPDATE player_statistics
        SET successfulBargains = successfulBargains + 1,
            totalBargains = totalBargains + 1
        WHERE playerId = :playerId
    """)
    suspend fun recordSuccessfulBargain(playerId: Int): Int

    @Query("UPDATE player_statistics SET totalBargains = totalBargains + 1 WHERE playerId = :playerId")
    suspend fun recordFailedBargain(playerId: Int): Int

    @Query("UPDATE player_statistics SET dailyRevenue = 0.0 WHERE playerId = :playerId")
    suspend fun resetDailyRevenue(playerId: Int): Int

    @Query("""
        UPDATE player_statistics
        SET highestProfit = :profit
        WHERE playerId = :playerId AND :profit > highestProfit
    """)
    suspend fun updateHighestProfitIfHigher(playerId: Int, profit: Double): Int

    @Query("UPDATE player_statistics SET rareItemsFound = rareItemsFound + 1 WHERE playerId = :playerId")
    suspend fun incrementRareItems(playerId: Int): Int

    @Query("SELECT dailyRevenue FROM player_statistics WHERE playerId = :playerId")
    suspend fun getDailyRevenue(playerId: Int): Double?

    @Query("DELETE FROM player_statistics WHERE playerId = :playerId")
    suspend fun deleteStatistics(playerId: Int): Int
}
