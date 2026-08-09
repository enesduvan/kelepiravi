package com.enesduvan.kelepiravi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.enesduvan.kelepiravi.database.entity.UserListingEntity
import kotlin.jvm.JvmSuppressWildcards
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface ListingDao {

    @Query("SELECT * FROM user_listings WHERE playerId = :playerId AND isActive = 1")
    fun observeActiveListings(playerId: Int): Flow<List<UserListingEntity>>

    @Query("SELECT * FROM user_listings WHERE playerId = :playerId AND isActive = 1")
    suspend fun getActiveListings(playerId: Int): List<UserListingEntity>

    @Query("SELECT * FROM user_listings WHERE playerId = :playerId")
    suspend fun getAllListings(playerId: Int): List<UserListingEntity>

    @Query("SELECT * FROM user_listings WHERE id = :id LIMIT 1")
    suspend fun getListingById(id: Long): UserListingEntity?

    @Query("SELECT * FROM user_listings WHERE id = :id AND playerId = :playerId LIMIT 1")
    suspend fun getListingByIdForPlayer(id: Long, playerId: Int): UserListingEntity?

    @Query("SELECT COUNT(*) FROM user_listings WHERE playerId = :playerId AND isActive = 1")
    suspend fun getActiveListingCount(playerId: Int): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertListing(listing: UserListingEntity): Long

    @Update
    suspend fun updateListing(listing: UserListingEntity): Int

    @Query("UPDATE user_listings SET askingPrice = :newPrice WHERE id = :id")
    suspend fun updatePrice(id: Long, newPrice: Double): Int

    @Query("UPDATE user_listings SET askingPrice = :newPrice, listingJson = :listingJson WHERE id = :id AND playerId = :playerId AND isActive = 1")
    suspend fun updatePriceForPlayer(id: Long, playerId: Int, newPrice: Double, listingJson: String): Int

    @Query("UPDATE user_listings SET isActive = 0 WHERE id = :id")
    suspend fun deactivateListing(id: Long): Int

    @Query("UPDATE user_listings SET isActive = 0 WHERE id = :id AND playerId = :playerId")
    suspend fun deactivateListingForPlayer(id: Long, playerId: Int): Int

    @Query("UPDATE user_listings SET isActive = 0 WHERE playerId = :playerId AND isActive = 1")
    suspend fun deactivateAllListings(playerId: Int): Int

    @Query("DELETE FROM user_listings WHERE id = :id")
    suspend fun deleteListing(id: Long): Int

    @Query("DELETE FROM user_listings WHERE playerId = :playerId")
    suspend fun deleteAllForPlayer(playerId: Int): Int
}
