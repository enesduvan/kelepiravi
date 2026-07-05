package com.enesduvan.kelepiravi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.enesduvan.kelepiravi.data.local.entity.UserInventoryEntity
import kotlin.jvm.JvmSuppressWildcards
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface KelepiraviDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(item: UserInventoryEntity): Long

    @Query("SELECT * FROM UserInventory")
    fun getAllInventories(): Flow<List<UserInventoryEntity>>

    @Query("SELECT * FROM UserInventory WHERE id = :id LIMIT 1")
    suspend fun getInventoryById(id: Int): UserInventoryEntity?

    @Update
    suspend fun updateInventory(item: UserInventoryEntity): Int

    @Delete
    suspend fun deleteInventory(item: UserInventoryEntity): Int

    @Query("DELETE FROM UserInventory")
    suspend fun deleteAll(): Int
}
