/*
 * App-QrCodeYar v2.0 - ذخیره Presetهای طراحی QR.
 *
 * فقط تنظیمات و URIهای persistable ذخیره می‌شوند؛ Bitmap داخل SharedPreferences قرار نمی‌گیرد.
 * حداکثر 20 Preset نگه‌داری می‌شود تا فایل تنظیمات کوچک و قابل Backup بماند.
 */
package com.waxew.qrbarcode.v20

import android.content.Context
import com.waxew.qrbarcode.generator.FinderStyle
import com.waxew.qrbarcode.generator.FrameStyle
import com.waxew.qrbarcode.generator.GradientDirection
import com.waxew.qrbarcode.generator.LogoShape
import com.waxew.qrbarcode.generator.ModuleStyle
import org.json.JSONArray
import org.json.JSONObject

data class V20DesignPreset(
    val name: String,
    val moduleStyle: ModuleStyle,
    val finderStyle: FinderStyle,
    val foreground: Int,
    val finderForeground: Int,
    val gradientEnd: Int,
    val gradientEnabled: Boolean,
    val gradientDirection: GradientDirection,
    val moduleScale: Float,
    val background: Int,
    val transparentBackground: Boolean,
    val backgroundImageUri: String?,
    val frameStyle: FrameStyle,
    val frameText: String,
    val logoUri: String?,
    val logoShape: LogoShape,
    val logoBorderColor: Int
)

class V20DesignPresetStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("qrcodeyar_design_presets_v2", Context.MODE_PRIVATE)

    fun all(): List<V20DesignPreset> {
        val array = runCatching { JSONArray(prefs.getString("items", "[]")) }.getOrElse { JSONArray() }
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                runCatching { decode(obj) }.getOrNull()?.let(::add)
            }
        }.take(20)
    }

    fun save(preset: V20DesignPreset) {
        val safe = preset.copy(name = preset.name.trim().take(30).ifBlank { "Preset" })
        val next = (listOf(safe) + all().filterNot { it.name.equals(safe.name, true) }).take(20)
        val array = JSONArray()
        next.forEach { array.put(encode(it)) }
        prefs.edit().putString("items", array.toString()).apply()
    }

    fun delete(name: String) {
        val array = JSONArray()
        all().filterNot { it.name.equals(name, true) }.forEach { array.put(encode(it)) }
        prefs.edit().putString("items", array.toString()).apply()
    }

    fun exportJson(): String = prefs.getString("items", "[]") ?: "[]"

    fun importJson(json: String) {
        val array = runCatching { JSONArray(json) }.getOrElse { JSONArray() }
        val validated = JSONArray()
        for (i in 0 until minOf(array.length(), 20)) {
            val obj = array.optJSONObject(i) ?: continue
            runCatching { decode(obj) }.getOrNull()?.let { validated.put(encode(it)) }
        }
        prefs.edit().putString("items", validated.toString()).apply()
    }

    private fun encode(item: V20DesignPreset) = JSONObject()
        .put("name", item.name)
        .put("moduleStyle", item.moduleStyle.name)
        .put("finderStyle", item.finderStyle.name)
        .put("foreground", item.foreground)
        .put("finderForeground", item.finderForeground)
        .put("gradientEnd", item.gradientEnd)
        .put("gradientEnabled", item.gradientEnabled)
        .put("gradientDirection", item.gradientDirection.name)
        .put("moduleScale", item.moduleScale.toDouble())
        .put("background", item.background)
        .put("transparentBackground", item.transparentBackground)
        .put("backgroundImageUri", item.backgroundImageUri ?: JSONObject.NULL)
        .put("frameStyle", item.frameStyle.name)
        .put("frameText", item.frameText)
        .put("logoUri", item.logoUri ?: JSONObject.NULL)
        .put("logoShape", item.logoShape.name)
        .put("logoBorderColor", item.logoBorderColor)

    private fun decode(obj: JSONObject) = V20DesignPreset(
        name = obj.optString("name").take(30).ifBlank { "Preset" },
        moduleStyle = enumValue(obj.optString("moduleStyle"), ModuleStyle.CLASSIC),
        finderStyle = enumValue(obj.optString("finderStyle"), FinderStyle.CLASSIC),
        foreground = obj.optInt("foreground", android.graphics.Color.rgb(42, 34, 55)),
        finderForeground = obj.optInt("finderForeground", obj.optInt("foreground", android.graphics.Color.rgb(42, 34, 55))),
        gradientEnd = obj.optInt("gradientEnd", android.graphics.Color.rgb(129, 103, 180)),
        gradientEnabled = obj.optBoolean("gradientEnabled", false),
        gradientDirection = enumValue(obj.optString("gradientDirection"), GradientDirection.DIAGONAL),
        moduleScale = obj.optDouble("moduleScale", 1.0).toFloat().coerceIn(0.55f, 1f),
        background = obj.optInt("background", android.graphics.Color.WHITE),
        transparentBackground = obj.optBoolean("transparentBackground", false),
        backgroundImageUri = obj.optString("backgroundImageUri").takeIf { it.isNotBlank() && it != "null" },
        frameStyle = enumValue(obj.optString("frameStyle"), FrameStyle.NONE),
        frameText = obj.optString("frameText").take(42),
        logoUri = obj.optString("logoUri").takeIf { it.isNotBlank() && it != "null" },
        logoShape = enumValue(obj.optString("logoShape"), LogoShape.ROUNDED),
        logoBorderColor = obj.optInt("logoBorderColor", android.graphics.Color.WHITE)
    )

    private inline fun <reified T : Enum<T>> enumValue(raw: String, fallback: T): T =
        runCatching { enumValueOf<T>(raw) }.getOrDefault(fallback)
}
