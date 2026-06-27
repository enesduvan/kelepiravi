package com.enesduvan.kelepiravi.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlin.jvm.JvmSuppressWildcards
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface KelepiraviDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(item: DatabaseKelepiravi): Long

    @Query("SELECT * FROM UserInventory")
    fun getAllInventories(): Flow<List<DatabaseKelepiravi>>

    @Query("SELECT * FROM UserInventory WHERE id = :id LIMIT 1")
    suspend fun getInventoryById(id: Int): DatabaseKelepiravi?

    @Update
    suspend fun updateInventory(item: DatabaseKelepiravi): Int

    @Delete
    suspend fun deleteInventory(item: DatabaseKelepiravi): Int

    @Query("DELETE FROM UserInventory")
    suspend fun deleteAll(): Int
}
