/*
 * App-QrCodeYar v1.9 - تنظیمات توسعه‌یافته و قابل Backup/Restore
 *
 * تنظیمات جدا از Room نگه‌داری می‌شوند تا ارتقا از نسخه‌های قبلی بدون Migration اجباری
 * انجام شود. همه داده‌ها محلی هستند. Snapshot متنی برای سازگاری با بکاپ‌های قدیمی حفظ شده است.
 */
package com.waxew.qrbarcode.v19

import android.content.Context

class V19SettingsRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("qrcodeyar_v19", Context.MODE_PRIVATE)

    var scannerBeep: Boolean
        get() = prefs.getBoolean("scanner_beep", true)
        set(value) = prefs.edit().putBoolean("scanner_beep", value).apply()

    var scannerVibrate: Boolean
        get() = prefs.getBoolean("scanner_vibrate", true)
        set(value) = prefs.edit().putBoolean("scanner_vibrate", value).apply()

    var continuousScan: Boolean
        get() = prefs.getBoolean("continuous_scan", false)
        set(value) = prefs.edit().putBoolean("continuous_scan", value).apply()

    var preventDuplicates: Boolean
        get() = prefs.getBoolean("prevent_duplicates", true)
        set(value) = prefs.edit().putBoolean("prevent_duplicates", value).apply()

    var confirmBeforeOpeningLinks: Boolean
        get() = prefs.getBoolean("confirm_links", true)
        set(value) = prefs.edit().putBoolean("confirm_links", value).apply()

    var compactMode: Boolean
        get() = prefs.getBoolean("compact_mode", false)
        set(value) = prefs.edit().putBoolean("compact_mode", value).apply()

    var accentName: String
        get() = prefs.getString("accent_name", "صورتی یاسی") ?: "صورتی یاسی"
        set(value) = prefs.edit().putString("accent_name", sanitizeAccent(value)).apply()

    var startPage: String
        get() = prefs.getString("start_page", "خانه") ?: "خانه"
        set(value) = prefs.edit().putString("start_page", sanitizeStartPage(value)).apply()

    var appLockEnabled: Boolean
        get() = prefs.getBoolean("app_lock", false)
        set(value) = prefs.edit().putBoolean("app_lock", value).apply()

    fun exportSnapshot(): String = buildString {
        appendLine("scannerBeep=$scannerBeep")
        appendLine("scannerVibrate=$scannerVibrate")
        appendLine("continuousScan=$continuousScan")
        appendLine("preventDuplicates=$preventDuplicates")
        appendLine("confirmBeforeOpeningLinks=$confirmBeforeOpeningLinks")
        appendLine("compactMode=$compactMode")
        appendLine("accentName=$accentName")
        appendLine("startPage=$startPage")
        appendLine("appLockEnabled=$appLockEnabled")
    }

    /**
     * Snapshot بکاپ را با لیست سفید کلیدها Restore می‌کند. مقادیر ناشناخته نادیده گرفته می‌شوند
     * تا فایل بکاپ نتواند SharedPreferences خارج از قرارداد برنامه را تغییر دهد.
     */
    fun importSnapshot(snapshot: String) {
        val values = snapshot.lineSequence()
            .mapNotNull { line ->
                val index = line.indexOf('=')
                if (index <= 0) null else line.substring(0, index).trim() to line.substring(index + 1).trim()
            }
            .toMap()

        values["scannerBeep"]?.toBooleanStrictOrNull()?.let { scannerBeep = it }
        values["scannerVibrate"]?.toBooleanStrictOrNull()?.let { scannerVibrate = it }
        values["continuousScan"]?.toBooleanStrictOrNull()?.let { continuousScan = it }
        values["preventDuplicates"]?.toBooleanStrictOrNull()?.let { preventDuplicates = it }
        values["confirmBeforeOpeningLinks"]?.toBooleanStrictOrNull()?.let { confirmBeforeOpeningLinks = it }
        values["compactMode"]?.toBooleanStrictOrNull()?.let { compactMode = it }
        values["accentName"]?.let { accentName = it }
        values["startPage"]?.let { startPage = it }
        // برای امنیت، appLockEnabled فقط در صورت وجود PIN واقعی در V19AppLock باید فعال شود؛
        // بنابراین Restore عمومی آن را به‌صورت خودکار روشن نمی‌کند.
    }

    private fun sanitizeAccent(value: String): String = when (value) {
        "سبز نعنایی", "آبی آسمانی", "صورتی یاسی" -> value
        else -> "صورتی یاسی"
    }

    private fun sanitizeStartPage(value: String): String = when (value) {
        "خانه", "اسکنر", "مرکز 1.9" -> value
        else -> "خانه"
    }
}
