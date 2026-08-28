/*
 * App-QrCodeYar - دیتابیس محلی تاریخچه
 *
 * Room جایگزین نگه‌داری تاریخچه در یک رشته JSON داخل SharedPreferences شده است. این ساختار
 * برای جستجو، Favorite، حذف و توسعه آینده (Folder/Tag) قابل اتکاتر است. دیتابیس کاملاً آفلاین
 * است و هیچ داده‌ای را Sync یا Upload نمی‌کند.
 */
package com.waxew.qrbarcode.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "history_items")
data class HistoryEntity(
    @PrimaryKey val createdAt: Long,
    val kind: String,
    val payload: String,
    val favorite: Boolean = false
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Query("SELECT COUNT(*) FROM history_items")
    suspend fun count(): Int

    @Query("SELECT * FROM history_items WHERE kind = :kind AND payload = :payload LIMIT 1")
    suspend fun find(kind: String, payload: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HistoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<HistoryEntity>)

    @Query("DELETE FROM history_items WHERE kind = :kind AND payload = :payload")
    suspend fun deleteMatching(kind: String, payload: String)

    @Query("DELETE FROM history_items WHERE createdAt = :createdAt")
    suspend fun delete(createdAt: Long)

    @Query("UPDATE history_items SET favorite = CASE favorite WHEN 1 THEN 0 ELSE 1 END WHERE createdAt = :createdAt")
    suspend fun toggleFavorite(createdAt: Long)

    @Query("DELETE FROM history_items")
    suspend fun clear()

    @Query("DELETE FROM history_items WHERE createdAt NOT IN (SELECT createdAt FROM history_items ORDER BY createdAt DESC LIMIT 100)")
    suspend fun trimToLatest100()
}

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var instance: HistoryDatabase? = null

        fun get(context: Context): HistoryDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                HistoryDatabase::class.java,
                "qrcodeyar_history.db"
            ).build().also { instance = it }
        }
    }
}
