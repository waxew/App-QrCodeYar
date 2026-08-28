/*
 * App-QrCodeYar v1.9 - تنظیمات توسعه‌یافته
 *
 * تنظیمات این نسخه جدا از تاریخچه Room نگه‌داری می‌شوند تا ارتقا از نسخه‌های قبلی
 * بدون Migration اجباری انجام شود. همه داده‌ها محلی هستند و به سرور ارسال نمی‌شوند.
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
        set(value) = prefs.edit().putString("accent_name", value).apply()

    var startPage: String
        get() = prefs.getString("start_page", "خانه") ?: "خانه"
        set(value) = prefs.edit().putString("start_page", value).apply()

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
}
