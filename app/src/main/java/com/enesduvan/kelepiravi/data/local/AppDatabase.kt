package com.enesduvan.kelepiravi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.enesduvan.kelepiravi.data.local.dao.KelepiraviDao
import com.enesduvan.kelepiravi.data.local.entity.UserInventoryEntity
import com.enesduvan.kelepiravi.data.model.MarketItemConverter

/**
 * Migration 1 → 2: currentDay kolonu eklendi.
 * ALTER TABLE basit bir sütun ekleme — mevcut veri korunur.
 */
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE UserInventory ADD COLUMN currentDay INTEGER NOT NULL DEFAULT 1"
        )
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN xp INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN level INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN totalProfit REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN itemsBought INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN itemsSold INTEGER NOT NULL DEFAULT 0")
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN unlockedAchievements TEXT NOT NULL DEFAULT ''")
    }
}

@Database(entities = [UserInventoryEntity::class], version = 4, exportSchema = false)
@TypeConverters(MarketItemConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kelepiraviDao(): KelepiraviDao
}

/** Thread-safe singleton. applicationContext ile çağrılmalı. */
object AppDatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "kelepiravi-database"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                .also { instance = it }
        }
    }
}
