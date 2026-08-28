/*
 * قفل محلی برنامه با PIN.
 * خود PIN ذخیره نمی‌شود؛ فقط SHA-256 آن در SharedPreferences نگه‌داری می‌شود.
 */
package com.waxew.qrbarcode.v19

import android.content.Context
import java.security.MessageDigest

class V19AppLock(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("qrcodeyar_v19_lock", Context.MODE_PRIVATE)

    val configured: Boolean
        get() = !prefs.getString("pin_hash", null).isNullOrBlank()

    fun setPin(pin: String): Boolean {
        if (!pin.matches(Regex("\\d{4,8}"))) return false
        prefs.edit().putString("pin_hash", hash(pin)).apply()
        return true
    }

    fun verify(pin: String): Boolean = prefs.getString("pin_hash", null) == hash(pin)

    fun clear() {
        prefs.edit().remove("pin_hash").apply()
    }

    private fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
