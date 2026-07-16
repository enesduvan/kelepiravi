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
import com.enesduvan.kelepiravi.data.model.ListingConverter
import com.enesduvan.kelepiravi.data.model.MarketItemConverter
import java.io.File

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

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN marketTrends TEXT NOT NULL DEFAULT ''")
    }
}

/**
 * Migration 5 → 6: Günlük tamir limiti kolonları eklendi.
 * dailyRepairsUsed: Bugün kullanılan tamir sayısı
 * lastRepairDay: Son tamir yapılan gün (sıfırlama için)
 */
private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN dailyRepairsUsed INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN lastRepairDay INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * Migration 6 → 7: dailyRevenue eklendi. (Bölüm 8 Ekonomi Dengesi)
 */
private val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN dailyRevenue REAL NOT NULL DEFAULT 0.0")
    }
}

/**
 * Migration 7 → 8: shopLevel ve mechanicLevel eklendi. (Bölüm 10 Yükseltmeler)
 */
private val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN shopLevel INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN mechanicLevel INTEGER NOT NULL DEFAULT 1")
    }
}

/**
 * Migration 8 → 9: eventFlags ve eventCooldowns eklendi. (Bölüm 2 Event Engine)
 */
private val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN eventFlags TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN eventCooldowns TEXT NOT NULL DEFAULT ''")
    }
}

/**
 * Migration 9 → 10: activeListings eklendi. (İlan Sistemi)
 */
private val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN activeListings TEXT NOT NULL DEFAULT '[]'")
    }
}

/**
 * Migration 10 → 11: npcRelationships eklendi. (NPC İlişki/Hafıza Sistemi)
 */
private val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN npcRelationships TEXT NOT NULL DEFAULT '{}'")
    }
}

/**
 * Migration 11 → 12: highestProfit ve rareItemsFound eklendi. (Gelişmiş İstatistikler)
 */
private val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN highestProfit REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN rareItemsFound INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * Migration 12 → 13: V5.0 Başarımlar (totalRepairs, hasBoughtScam, hasBoughtAbsurd)
 */
private val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN totalRepairs INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN hasBoughtScam INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN hasBoughtAbsurd INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * Migration 13 → 14: activeModifiers eklendi. (Continuous Events)
 */
private val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN activeModifiers TEXT NOT NULL DEFAULT '{}'")
    }
}

/**
 * Migration 14 → 15: V6.0 İstatistikleri (successfulBargains, totalBargains, soldCategories)
 */
private val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN successfulBargains INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN totalBargains INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE UserInventory ADD COLUMN soldCategories TEXT NOT NULL DEFAULT '{}'")
    }
}

@Database(entities = [UserInventoryEntity::class], version = 15, exportSchema = false)
@TypeConverters(MarketItemConverter::class, ListingConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kelepiraviDao(): KelepiraviDao
}

/** Thread-safe singleton. applicationContext ile çağrılmalı. */
object AppDatabaseProvider {
    private const val DATABASE_NAME = "kelepiravi-database"
    private const val BACKUP_DIR = "database-backups"
    private const val BACKUP_EXTENSION = ".bak"

    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        val appContext = context.applicationContext
        restoreBackupIfPrimaryMissing(appContext)
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                    MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                    MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                    MIGRATION_13_14, MIGRATION_14_15
                ).addCallback(DatabaseBackupCallback(appContext))
                .build()
                .also { instance = it }
        }
    }

    private class DatabaseBackupCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            backupDatabase(context)
        }
    }

    private fun backupDatabase(context: Context) {
        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        if (!databaseFile.exists()) return

        val backupDirectory = File(context.filesDir, BACKUP_DIR).apply { mkdirs() }
        databaseFiles(databaseFile).forEach { source ->
            if (source.exists()) {
                runCatching {
                    source.copyTo(
                        target = File(backupDirectory, source.name + BACKUP_EXTENSION),
                        overwrite = true
                    )
                }
            }
        }
    }

    private fun restoreBackupIfPrimaryMissing(context: Context) {
        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        if (databaseFile.exists()) return

        val backupDirectory = File(context.filesDir, BACKUP_DIR)
        val primaryBackup = File(backupDirectory, DATABASE_NAME + BACKUP_EXTENSION)
        if (!primaryBackup.exists()) return

        databaseFile.parentFile?.mkdirs()
        databaseFiles(databaseFile).forEach { target ->
            val backup = File(backupDirectory, target.name + BACKUP_EXTENSION)
            if (backup.exists()) {
                runCatching {
                    backup.copyTo(target = target, overwrite = true)
                }
            }
        }
    }

    private fun databaseFiles(databaseFile: File): List<File> {
        return listOf(
            databaseFile,
            File(databaseFile.path + "-wal"),
            File(databaseFile.path + "-shm")
        )
    }
}
