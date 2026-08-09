package com.enesduvan.kelepiravi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.enesduvan.kelepiravi.database.entity.UserInventoryItemEntity
import kotlin.jvm.JvmSuppressWildcards
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface InventoryItemDao {

    @Query("SELECT * FROM user_inventory_items WHERE playerId = :playerId")
    fun observeInventory(playerId: Int): Flow<List<UserInventoryItemEntity>>

    @Query("SELECT * FROM user_inventory_items WHERE playerId = :playerId")
    suspend fun getInventory(playerId: Int): List<UserInventoryItemEntity>

    @Query("SELECT * FROM user_inventory_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): UserInventoryItemEntity?

    @Query("SELECT * FROM user_inventory_items WHERE id = :id AND playerId = :playerId LIMIT 1")
    suspend fun getItemByIdForPlayer(id: Long, playerId: Int): UserInventoryItemEntity?

    @Query("SELECT * FROM user_inventory_items WHERE playerId = :playerId AND itemId = :itemId")
    suspend fun getItemsByItemId(playerId: Int, itemId: String): List<UserInventoryItemEntity>

    @Query("SELECT COUNT(*) FROM user_inventory_items WHERE playerId = :playerId")
    suspend fun getInventoryCount(playerId: Int): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItem(item: UserInventoryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<UserInventoryItemEntity>): List<Long>

    @Update
    suspend fun updateItem(item: UserInventoryItemEntity): Int

    @Query("UPDATE user_inventory_items SET condition = :condition WHERE id = :id")
    suspend fun updateCondition(id: Long, condition: Int): Int

    @Query("UPDATE user_inventory_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Int): Int

    @Query("DELETE FROM user_inventory_items WHERE id = :id")
    suspend fun deleteItem(id: Long): Int

    @Query("DELETE FROM user_inventory_items WHERE id = :id AND playerId = :playerId")
    suspend fun deleteItemForPlayer(id: Long, playerId: Int): Int

    @Query("DELETE FROM user_inventory_items WHERE playerId = :playerId")
    suspend fun deleteAllForPlayer(playerId: Int): Int
}
