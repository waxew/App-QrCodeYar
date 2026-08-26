/*
 * ذخیره‌سازی سبک و کاملاً محلی تنظیمات و تاریخچه با SharedPreferences.
 * هیچ اطلاعات تاریخچه‌ای برای سرور ارسال نمی‌شود.
 */
package com.waxew.qrbarcode.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// یک رکورد تاریخچه شامل نوع عملیات، payload کوتاه‌شده و زمان ایجاد.
data class HistoryItem(val kind: String, val payload: String, val createdAt: Long)

class PreferencesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("qr_studio_preferences", Context.MODE_PRIVATE)

    // تنظیم اعلان‌ها؛ مقدار پیش‌فرض روشن است.
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications_enabled", true)
        set(value) = prefs.edit().putBoolean("notifications_enabled", value).apply()

    // رکورد جدید را اول لیست می‌گذارد، موارد تکراری را حذف و حداکثر ۳۰ مورد نگه می‌دارد.
    fun addHistory(kind: String, payload: String) {
        val items = history().toMutableList()
        items.add(0, HistoryItem(kind, payload.take(300), System.currentTimeMillis()))
        val limited = items.distinctBy { "${it.kind}:${it.payload}" }.take(30)
        val array = JSONArray()
        limited.forEach { item ->
            array.put(JSONObject().apply {
                put("kind", item.kind)
                put("payload", item.payload)
                put("createdAt", item.createdAt)
            })
        }
        prefs.edit().putString("history", array.toString()).apply()
    }

    // JSON ذخیره‌شده را با تحمل خطا به مدل‌های HistoryItem تبدیل می‌کند.
    fun history(): List<HistoryItem> {
        val raw = prefs.getString("history", "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(HistoryItem(obj.optString("kind"), obj.optString("payload"), obj.optLong("createdAt")))
                }
            }
        }.getOrDefault(emptyList())
    }
}
