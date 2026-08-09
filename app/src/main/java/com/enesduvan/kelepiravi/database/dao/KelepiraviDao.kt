package com.enesduvan.kelepiravi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.enesduvan.kelepiravi.database.entity.UserInventoryEntity
import kotlin.jvm.JvmSuppressWildcards
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface KelepiraviDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(item: UserInventoryEntity): Long

    @Query("SELECT * FROM UserInventory")
    fun getAllInventories(): Flow<List<UserInventoryEntity>>

    @Query("SELECT * FROM UserInventory WHERE playerId = :playerId LIMIT 1")
    suspend fun getInventoryById(playerId: Int): UserInventoryEntity?

    @Update
    suspend fun updateInventory(item: UserInventoryEntity): Int

    @Query("DELETE FROM UserInventory")
    suspend fun deleteAll(): Int
}
