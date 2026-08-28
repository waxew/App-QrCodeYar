/*
 * App-QrCodeYar - تنظیمات سبک + Repository تاریخچه Room
 *
 * پروفایل و Toggleهای ساده در SharedPreferences می‌مانند، اما History در Room ذخیره می‌شود.
 * هنگام اولین اجرای نسخه 1.1، تاریخچه JSON نسخه 1.0.1 یک‌بار به Room منتقل می‌شود؛ بنابراین
 * Update برنامه باعث از دست رفتن داده‌های قبلی کاربر نمی‌شود.
 *
 * history() برای سازگاری UI فعلی یک Cache درون‌حافظه‌ای از Flow دیتابیس برمی‌گرداند؛ تغییرات
 * کاربر ابتدا به‌صورت Optimistic روی Cache اعمال و سپس روی Dispatcher.IO در Room ثبت می‌شوند.
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
    val favorite: Boolean = false
)

class PreferencesRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("qr_studio_preferences", Context.MODE_PRIVATE)
    private val dao = HistoryDatabase.get(appContext).historyDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile private var cachedHistory: List<HistoryItem> = emptyList()

    val historyFlow: Flow<List<HistoryItem>> = dao.observeAll().map { rows ->
        rows.map { HistoryItem(it.kind, it.payload, it.createdAt, it.favorite) }
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
        val newItem = HistoryItem(kind, safePayload, now, previous?.favorite ?: false)
        cachedHistory = buildList {
            add(newItem)
            addAll(cachedHistory.filterNot { it.kind == kind && it.payload == safePayload })
        }.take(100)

        scope.launch {
            dao.deleteMatching(kind, safePayload)
            dao.insert(HistoryEntity(now, kind, safePayload, newItem.favorite))
            dao.trimToLatest100()
        }
    }

    fun toggleFavorite(createdAt: Long) {
        cachedHistory = cachedHistory.map { item ->
            if (item.createdAt == createdAt) item.copy(favorite = !item.favorite) else item
        }
        scope.launch { dao.toggleFavorite(createdAt) }
    }

    fun removeHistory(createdAt: Long) {
        cachedHistory = cachedHistory.filterNot { it.createdAt == createdAt }
        scope.launch { dao.delete(createdAt) }
    }

    fun clearHistory() {
        cachedHistory = emptyList()
        scope.launch { dao.clear() }
    }

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
                                    kind = obj.optString("kind"),
                                    payload = payload,
                                    createdAt = obj.optLong("createdAt").takeIf { it > 0 }
                                        ?: (System.currentTimeMillis() + i),
                                    favorite = obj.optBoolean("favorite", false)
                                )
                            )
                        }
                    }.take(100)
                    dao.insertAll(oldRows)
                    dao.trimToLatest100()
                }
                prefs.edit().putBoolean("history_room_migrated_v1", true).apply()
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}
