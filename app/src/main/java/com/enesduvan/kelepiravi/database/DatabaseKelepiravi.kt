package com.enesduvan.kelepiravi.database

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.enesduvan.kelepiravi.MarketItem
import com.enesduvan.kelepiravi.MarketItemConverter

const val DEFAULT_USER_ID = 1
const val INITIAL_BALANCE = "25000.0"

@Entity(tableName = "UserInventory")
data class DatabaseKelepiravi(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val balance: String,
    val inventory: List<MarketItem>
)

@Database(entities = [DatabaseKelepiravi::class], version = 1, exportSchema = false)
@TypeConverters(MarketItemConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kelepiraviDao(): KelepiraviDao
}

object KelepiraviDatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "kelepiravi-database"
            ).build().also { instance = it }
        }
    }
}
