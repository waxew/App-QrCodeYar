/*
 * App-QrCodeYar - دیتابیس محلی تاریخچه و آرشیو
 *
 * نسخه 2 دیتابیس، Folder و Tag را به هر رکورد اضافه می‌کند. Migration از نسخه 1 فقط دو
 * ستون جدید با مقدار خالی می‌سازد؛ بنابراین هیچ تاریخچه، Favorite یا زمان ثبت قبلی حذف نمی‌شود.
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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "history_items")
data class HistoryEntity(
    @PrimaryKey val createdAt: Long,
    val kind: String,
    val payload: String,
    val favorite: Boolean = false,
    val folder: String = "",
    val tags: String = ""
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

    @Query("UPDATE history_items SET folder = :folder, tags = :tags WHERE createdAt = :createdAt")
    suspend fun updateArchiveMetadata(createdAt: Long, folder: String, tags: String)

    @Query("SELECT DISTINCT folder FROM history_items WHERE folder != '' ORDER BY folder COLLATE NOCASE")
    suspend fun folders(): List<String>

    @Query("DELETE FROM history_items")
    suspend fun clear()

    @Query("DELETE FROM history_items WHERE createdAt NOT IN (SELECT createdAt FROM history_items ORDER BY createdAt DESC LIMIT 500)")
    suspend fun trimToLatest500()
}

@Database(entities = [HistoryEntity::class], version = 2, exportSchema = false)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var instance: HistoryDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE history_items ADD COLUMN folder TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE history_items ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
            }
        }

        fun get(context: Context): HistoryDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                HistoryDatabase::class.java,
                "qrcodeyar_history.db"
            ).addMigrations(MIGRATION_1_2)
                .build()
                .also { instance = it }
        }
    }
}
