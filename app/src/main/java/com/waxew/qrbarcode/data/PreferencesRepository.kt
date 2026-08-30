/*
 * App-QrCodeYar - تنظیمات سبک + Repository تاریخچه Room
 *
 * History در Room ذخیره می‌شود. نسخه 1.9 علاوه بر Favorite، Folder و Tag واقعی دارد.
 * مهاجرت JSON قدیمی و Restore بکاپ جدید داده کاربر را هنگام Update حفظ می‌کنند.
 */
package com.waxew.qrbarcode.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray

data class HistoryItem(
    val kind: String,
    val payload: String,
    val createdAt: Long,
    val favorite: Boolean = false,
    val folder: String = "",
    val tags: String = ""
)

class PreferencesRepository(context: Context) {
    val appContext: Context = context.applicationContext
    private val prefs = appContext.getSharedPreferences("qr_studio_preferences", Context.MODE_PRIVATE)
    private val dao = HistoryDatabase.get(appContext).historyDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile private var cachedHistory: List<HistoryItem> = emptyList()

    val historyFlow: Flow<List<HistoryItem>> = dao.observeAll().map { rows ->
        rows.map { HistoryItem(it.kind, it.payload, it.createdAt, it.favorite, it.folder, it.tags) }
    }

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications_enabled", true)
        set(value) = prefs.edit().putBoolean("notifications_enabled", value).apply()

    var profileName: String
        get() = prefs.getString("profile_name", "کاربر")?.ifBlank { "کاربر" } ?: "کاربر"
        set(value) = prefs.edit().putString("profile_name", value.trim().take(40)).apply()

    var profileImageUri: String?
        get() = prefs.getString("profile_image_uri", null)
        set(value) = prefs.edit().putString("profile_image_uri", value).apply()

    init {
        scope.launch { historyFlow.collectLatest { cachedHistory = it } }
        migrateLegacyHistoryOnce()
    }

    fun history(): List<HistoryItem> = cachedHistory

    fun addHistory(kind: String, payload: String) {
        val safePayload = payload.take(1000)
        if (safePayload.isBlank()) return
        val now = System.currentTimeMillis()
        val previous = cachedHistory.firstOrNull { it.kind == kind && it.payload == safePayload }
        val newItem = HistoryItem(
            kind = kind,
            payload = safePayload,
            createdAt = now,
            favorite = previous?.favorite ?: false,
            folder = previous?.folder.orEmpty(),
            tags = previous?.tags.orEmpty()
        )
        cachedHistory = buildList {
            add(newItem)
            addAll(cachedHistory.filterNot { it.kind == kind && it.payload == safePayload })
        }.take(500)

        scope.launch {
            dao.deleteMatching(kind, safePayload)
            dao.insert(HistoryEntity(now, kind, safePayload, newItem.favorite, newItem.folder, newItem.tags))
            dao.trimToLatest500()
        }
    }

    fun toggleFavorite(createdAt: Long) {
        cachedHistory = cachedHistory.map { item ->
            if (item.createdAt == createdAt) item.copy(favorite = !item.favorite) else item
        }
        scope.launch { dao.toggleFavorite(createdAt) }
    }

    fun updateArchiveMetadata(createdAt: Long, folder: String, tags: String) {
        val safeFolder = folder.trim().take(40)
        val safeTags = normalizeTags(tags)
        cachedHistory = cachedHistory.map { item ->
            if (item.createdAt == createdAt) item.copy(folder = safeFolder, tags = safeTags) else item
        }
        scope.launch { dao.updateArchiveMetadata(createdAt, safeFolder, safeTags) }
    }

    fun folders(): List<String> = cachedHistory.map { it.folder }.filter { it.isNotBlank() }.distinct().sorted()

    fun removeHistory(createdAt: Long) {
        cachedHistory = cachedHistory.filterNot { it.createdAt == createdAt }
        scope.launch { dao.delete(createdAt) }
    }

    fun clearHistory() {
        cachedHistory = emptyList()
        scope.launch { dao.clear() }
    }

    /**
     * تاریخچه یک بکاپ معتبر را جایگزین تاریخچه فعلی می‌کند. قبل از نوشتن، رکوردها sanitize
     * و روی createdAt یکتا می‌شوند تا Primary Key دیتابیس شکسته نشود.
     */
    fun restoreHistory(items: List<HistoryItem>) {
        val sanitized = items.asSequence()
            .mapIndexed { index, item ->
                HistoryItem(
                    kind = item.kind.trim().take(40),
                    payload = item.payload.trim().take(1000),
                    createdAt = item.createdAt.takeIf { it > 0 } ?: (System.currentTimeMillis() + index),
                    favorite = item.favorite,
                    folder = item.folder.trim().take(40),
                    tags = normalizeTags(item.tags)
                )
            }
            .filter { it.payload.isNotBlank() }
            .distinctBy { it.createdAt }
            .sortedByDescending { it.createdAt }
            .take(500)
            .toList()

        cachedHistory = sanitized
        scope.launch {
            dao.clear()
            dao.insertAll(sanitized.map {
                HistoryEntity(it.createdAt, it.kind, it.payload, it.favorite, it.folder, it.tags)
            })
            dao.trimToLatest500()
        }
    }

    private fun normalizeTags(tags: String): String = tags.split(',')
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase() }
        .take(12)
        .joinToString(",") { it.take(24) }

    private fun migrateLegacyHistoryOnce() {
        if (prefs.getBoolean("history_room_migrated_v1", false)) return
        scope.launch {
            runCatching {
                if (dao.count() == 0) {
                    val raw = prefs.getString("history", "[]") ?: "[]"
                    val array = JSONArray(raw)
                    val oldRows = buildList {
                        for (i in 0 until array.length()) {
                            val obj = array.optJSONObject(i) ?: continue
                            val payload = obj.optString("payload").take(1000)
                            if (payload.isBlank()) continue
                            add(
                                HistoryEntity(
                                    kind = obj.optString("kind").take(40),
                                    payload = payload,
                                    createdAt = obj.optLong("createdAt").takeIf { it > 0 }
                                        ?: (System.currentTimeMillis() + i),
                                    favorite = obj.optBoolean("favorite", false),
                                    folder = obj.optString("folder", "").take(40),
                                    tags = normalizeTags(obj.optString("tags", ""))
                                )
                            )
                        }
                    }.take(500)
                    dao.insertAll(oldRows)
                    dao.trimToLatest500()
                }
                prefs.edit().putBoolean("history_room_migrated_v1", true).apply()
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}
