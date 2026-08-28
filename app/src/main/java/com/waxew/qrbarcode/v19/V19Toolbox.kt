/*
 * App-QrCodeYar v1.9 - منطق مستقل از UI برای قالب‌ها، امنیت، لیبل و Backup/Restore.
 */
package com.waxew.qrbarcode.v19

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.waxew.qrbarcode.data.HistoryItem
import com.waxew.qrbarcode.generator.CodeGenerator
import com.waxew.qrbarcode.util.NumberFormatter
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class SmartTemplate(
    val id: String,
    val title: String,
    val emoji: String,
    val hint: String,
    val premium: Boolean
)

object SmartTemplateCatalog {
    val items = listOf(
        SmartTemplate("wifi", "کارت Wi-Fi", "📶", "نام شبکه و رمز را وارد کن", false),
        SmartTemplate("business", "کارت ویزیت", "🪪", "نام، تلفن، ایمیل و وب‌سایت", true),
        SmartTemplate("restaurant", "منوی رستوران", "🍽️", "لینک منوی آنلاین یا PDF", true),
        SmartTemplate("instagram", "شبکه اجتماعی", "💗", "لینک صفحه یا پروفایل", true),
        SmartTemplate("product", "کارت محصول", "🏷️", "نام، قیمت و شناسه کالا", true),
        SmartTemplate("location", "موقعیت مکانی", "📍", "مختصات یا لینک نقشه", false)
    )

    /** قالب انتخاب‌شده را به Payload استاندارد QR تبدیل می‌کند. */
    fun payloadFor(id: String, fields: Map<String, String>): String = when (id) {
        "wifi" -> {
            val ssid = escapeWifi(fields["ssid"].orEmpty())
            val password = escapeWifi(fields["password"].orEmpty())
            val type = fields["type"].orEmpty().uppercase().takeIf { it in setOf("WPA", "WEP", "NOPASS") } ?: "WPA"
            "WIFI:T:$type;S:$ssid;P:$password;;"
        }
        "business" -> buildString {
            append("BEGIN:VCARD\nVERSION:3.0\n")
            append("FN:${clean(fields["name"])}\n")
            fields["phone"]?.takeIf { it.isNotBlank() }?.let { append("TEL:${clean(it)}\n") }
            fields["email"]?.takeIf { it.isNotBlank() }?.let { append("EMAIL:${clean(it)}\n") }
            fields["url"]?.takeIf { it.isNotBlank() }?.let { append("URL:${clean(it)}\n") }
            append("END:VCARD")
        }
        "restaurant" -> clean(fields["url"])
        "instagram" -> normalizeWebUrl(fields["url"].orEmpty())
        "product" -> buildString {
            append(clean(fields["name"]))
            fields["price"]?.takeIf { it.isNotBlank() }?.let { append("\nقیمت: ${clean(it)}") }
            fields["code"]?.takeIf { it.isNotBlank() }?.let { append("\nکد: ${clean(it)}") }
            fields["url"]?.takeIf { it.isNotBlank() }?.let { append("\n${normalizeWebUrl(it)}") }
        }
        "location" -> {
            val lat = fields["lat"].orEmpty().trim()
            val lon = fields["lon"].orEmpty().trim()
            if (lat.toDoubleOrNull() != null && lon.toDoubleOrNull() != null) "geo:$lat,$lon" else clean(fields["url"])
        }
        else -> clean(fields["text"])
    }.take(4000)

    private fun clean(value: String?): String = value.orEmpty().trim().replace("\u0000", "").take(1000)
    private fun normalizeWebUrl(value: String): String {
        val text = clean(value)
        return if (text.isBlank() || text.contains("://")) text else "https://$text"
    }
    private fun escapeWifi(value: String): String = clean(value)
        .replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace(":", "\\:")
}

data class V19LinkAssessment(val risky: Boolean, val score: Int, val reasons: List<String>)

object V19LinkSecurity {
    fun assess(raw: String): V19LinkAssessment {
        val text = raw.trim()
        val lower = text.lowercase()
        val reasons = mutableListOf<String>()
        var score = 0
        if (lower.startsWith("javascript:") || lower.startsWith("file:") || lower.startsWith("intent:")) {
            return V19LinkAssessment(true, 100, listOf("Scheme پرخطر"))
        }
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) return V19LinkAssessment(false, 0, emptyList())
        if (lower.startsWith("http://")) { score += 20; reasons += "ارتباط بدون HTTPS" }
        if (lower.contains("@")) { score += 25; reasons += "وجود @ در آدرس" }
        if (lower.contains("xn--")) { score += 20; reasons += "دامنه Punycode" }
        if (Regex("https?://(?:\\d{1,3}\\.){3}\\d{1,3}").containsMatchIn(lower)) { score += 25; reasons += "استفاده مستقیم از IP" }
        if (listOf("bit.ly", "tinyurl.com", "t.co", "goo.gl").any { lower.contains(it) }) { score += 15; reasons += "لینک کوتاه‌شده" }
        if (lower.contains("localhost") || lower.contains("127.0.0.1")) { score += 35; reasons += "آدرس محلی دستگاه" }
        return V19LinkAssessment(score >= 30, score.coerceAtMost(100), reasons)
    }
}

data class ProductLabelSpec(
    val title: String,
    val price: Long,
    val code: String,
    val format: BarcodeFormat = BarcodeFormat.CODE_128,
    val showPrice: Boolean = true,
    val showCode: Boolean = true
)

object ProductLabelRenderer {
    fun render(spec: ProductLabelSpec, width: Int = 1400, height: Int = 760): Bitmap {
        require(width in 600..3000 && height in 320..2000) { "ابعاد لیبل خارج از محدوده مجاز است." }
        val barcode = CodeGenerator.barcode(spec.code, spec.format, width = width - 120, height = (height * 0.48f).toInt())
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(42, 34, 55); textSize = width * 0.039f; textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val secondaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(70, 65, 80); textSize = width * 0.027f; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(spec.title.take(48), width / 2f, height * 0.11f, titlePaint)
        canvas.drawBitmap(barcode.bitmap, 60f, height * 0.16f, null)
        if (spec.showPrice) canvas.drawText("${NumberFormatter.groupInteger(spec.price)} تومان", width / 2f, height * 0.82f, titlePaint)
        if (spec.showCode) canvas.drawText(spec.code.take(64), width / 2f, height * 0.92f, secondaryPaint)
        barcode.bitmap.recycle()
        return result
    }
}

data class V19BackupPayload(val settingsSnapshot: String, val history: List<HistoryItem>)

object V19BackupManager {
    fun buildJson(history: List<HistoryItem>, settings: V19SettingsRepository): String {
        val root = JSONObject().put("schema", 2).put("settings", settings.exportSnapshot())
        val array = JSONArray()
        history.take(500).forEach { item ->
            array.put(JSONObject()
                .put("kind", item.kind)
                .put("payload", item.payload)
                .put("createdAt", item.createdAt)
                .put("favorite", item.favorite)
                .put("folder", item.folder)
                .put("tags", item.tags))
        }
        root.put("history", array)
        return root.toString(2)
    }

    fun parseJson(json: String): V19BackupPayload {
        require(json.toByteArray(Charsets.UTF_8).size <= 2_000_000) { "فایل بکاپ بیش از حد بزرگ است." }
        val root = JSONObject(json)
        val schema = root.optInt("schema", 1)
        require(schema in 1..2) { "نسخه بکاپ پشتیبانی نمی‌شود." }
        val array = root.optJSONArray("history") ?: JSONArray()
        val history = buildList {
            for (i in 0 until minOf(array.length(), 500)) {
                val obj = array.optJSONObject(i) ?: continue
                val payload = obj.optString("payload").take(1000)
                if (payload.isBlank()) continue
                add(HistoryItem(
                    kind = obj.optString("kind").take(40),
                    payload = payload,
                    createdAt = obj.optLong("createdAt").takeIf { it > 0 } ?: System.currentTimeMillis() + i,
                    favorite = obj.optBoolean("favorite", false),
                    folder = obj.optString("folder").take(40),
                    tags = obj.optString("tags").take(300)
                ))
            }
        }
        return V19BackupPayload(root.optString("settings"), history)
    }

    fun saveToCache(context: Context, json: String): File {
        val file = File(context.cacheDir, "QrCodeYar-backup-${System.currentTimeMillis()}.json")
        file.writeText(json, Charsets.UTF_8)
        return file
    }
}

data class ArchiveFolder(val id: String, val title: String)
data class ArchiveTag(val id: String, val title: String)
