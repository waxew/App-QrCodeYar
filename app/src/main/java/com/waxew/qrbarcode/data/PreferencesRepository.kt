/*
 * App-QrCodeYar - مخزن تنظیمات و داده‌های سبک محلی
 *
 * این کلاس اطلاعاتی را نگه می‌دارد که برایشان دیتابیس کامل لازم نیست: تنظیم اعلان‌ها،
 * مشخصات نمایشی پروفایل Drawer و تاریخچه QR/Barcode/Scan. تمام داده‌ها روی خود گوشی
 * در SharedPreferences ذخیره می‌شوند و هیچ محتوای تاریخچه‌ای به سرور ارسال نمی‌شود.
 *
 * نکته مهاجرت: ساختار JSON تاریخچه طوری خوانده می‌شود که رکوردهای نسخه‌های قدیمی که
 * فیلد favorite ندارند نیز بدون خطا باز شوند و favorite آن‌ها false در نظر گرفته شود.
 */
package com.waxew.qrbarcode.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// یک رکورد تاریخچه. createdAt شناسه پایدار محلی هم محسوب می‌شود.
data class HistoryItem(
    val kind: String,
    val payload: String,
    val createdAt: Long,
    val favorite: Boolean = false
)

class PreferencesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("qr_studio_preferences", Context.MODE_PRIVATE)

    // تنظیم اعلان‌ها؛ مقدار پیش‌فرض روشن است.
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications_enabled", true)
        set(value) = prefs.edit().putBoolean("notifications_enabled", value).apply()

    // نام نمایشی کاربر در بالای منوی همبرگری. این نام حساب کاربری آنلاین نیست.
    var profileName: String
        get() = prefs.getString("profile_name", "کاربر")?.ifBlank { "کاربر" } ?: "کاربر"
        set(value) = prefs.edit().putString("profile_name", value.trim().take(40)).apply()

    // URI تصویر پروفایل انتخاب‌شده از Storage Access Framework؛ فقط URI نگه‌داری می‌شود.
    var profileImageUri: String?
        get() = prefs.getString("profile_image_uri", null)
        set(value) = prefs.edit().putString("profile_image_uri", value).apply()

    // رکورد جدید را ابتدای فهرست قرار می‌دهد، تکراری‌ها را یکی می‌کند و تا ۱۰۰ مورد نگه می‌دارد.
    // اگر رکورد مشابه قبلاً Favorite بوده باشد، وضعیت Favorite در رکورد تازه حفظ می‌شود.
    fun addHistory(kind: String, payload: String) {
        val safePayload = payload.take(1000)
        val oldItems = history()
        val oldFavorite = oldItems.firstOrNull { it.kind == kind && it.payload == safePayload }?.favorite ?: false
        val newItem = HistoryItem(
            kind = kind,
            payload = safePayload,
            createdAt = System.currentTimeMillis(),
            favorite = oldFavorite
        )

        val merged = buildList {
            add(newItem)
            addAll(oldItems.filterNot { it.kind == kind && it.payload == safePayload })
        }.take(100)

        saveHistory(merged)
    }

    // فهرست کامل تاریخچه را از JSON محلی می‌خواند.
    fun history(): List<HistoryItem> {
        val raw = prefs.getString("history", "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        HistoryItem(
                            kind = obj.optString("kind"),
                            payload = obj.optString("payload"),
                            createdAt = obj.optLong("createdAt"),
                            favorite = obj.optBoolean("favorite", false)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    // Favorite یک رکورد را با استفاده از createdAt تغییر می‌دهد.
    fun toggleFavorite(createdAt: Long) {
        saveHistory(
            history().map { item ->
                if (item.createdAt == createdAt) item.copy(favorite = !item.favorite) else item
            }
        )
    }

    // حذف تک‌رکوردی برای مدیریت تاریخچه.
    fun removeHistory(createdAt: Long) {
        saveHistory(history().filterNot { it.createdAt == createdAt })
    }

    // پاک‌کردن تاریخچه؛ تنظیمات و پروفایل دست‌نخورده می‌مانند.
    fun clearHistory() {
        prefs.edit().remove("history").apply()
    }

    // نقطه مشترک Serializing تاریخچه به JSON.
    private fun saveHistory(items: List<HistoryItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("kind", item.kind)
                    put("payload", item.payload)
                    put("createdAt", item.createdAt)
                    put("favorite", item.favorite)
                }
            )
        }
        prefs.edit().putString("history", array.toString()).apply()
    }
}
