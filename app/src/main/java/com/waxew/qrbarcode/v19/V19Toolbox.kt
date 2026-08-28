/*
 * App-QrCodeYar v1.9 - جعبه ابزار قابلیت‌های نسخه‌های 1.2 تا 1.9
 *
 * این فایل منطق مستقل از UI را برای قالب‌های آماده، برچسب فروشگاهی، آرشیو،
 * سیاست امنیت لینک و بکاپ محلی فراهم می‌کند. هدف این است که قابلیت‌ها قابل تست و توسعه باشند.
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

// قالب آماده برای سناریوهای پرتکرار مثل Wi-Fi، رستوران، شبکه اجتماعی و محصول.
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
}

// نتیجه بررسی URL قبل از بازشدن توسط کاربر.
data class V19LinkAssessment(
    val risky: Boolean,
    val score: Int,
    val reasons: List<String>
)

object V19LinkSecurity {
    fun assess(raw: String): V19LinkAssessment {
        val text = raw.trim()
        if (!text.startsWith("http://", true) && !text.startsWith("https://", true)) {
            return V19LinkAssessment(false, 0, emptyList())
        }
        val reasons = mutableListOf<String>()
        var score = 0
        val lower = text.lowercase()
        if (lower.startsWith("http://")) {
            score += 20
            reasons += "ارتباط بدون HTTPS"
        }
        if (lower.contains("@")) {
            score += 25
            reasons += "وجود @ در آدرس"
        }
        if (lower.contains("xn--")) {
            score += 20
            reasons += "دامنه Punycode"
        }
        if (Regex("https?://(?:\\d{1,3}\\.){3}\\d{1,3}").containsMatchIn(lower)) {
            score += 25
            reasons += "استفاده مستقیم از IP"
        }
        if (listOf("bit.ly", "tinyurl.com", "t.co", "goo.gl").any { lower.contains(it) }) {
            score += 15
            reasons += "لینک کوتاه‌شده"
        }
        if (listOf("javascript:", "file:", "intent:").any { lower.startsWith(it) }) {
            score += 100
            reasons += "Scheme پرخطر"
        }
        return V19LinkAssessment(score >= 30, score.coerceAtMost(100), reasons)
    }
}

// مدل طراحی برچسب فروشگاهی.
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
        val barcode = CodeGenerator.barcode(spec.code, spec.format, width = width - 120, height = 360)
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(42, 34, 55)
            textSize = 54f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val secondaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(70, 65, 80)
            textSize = 38f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(spec.title.take(48), width / 2f, 80f, titlePaint)
        canvas.drawBitmap(barcode.bitmap, 60f, 125f, null)
        if (spec.showPrice) {
            canvas.drawText("${NumberFormatter.groupInteger(spec.price)} تومان", width / 2f, 600f, titlePaint)
        }
        if (spec.showCode) {
            canvas.drawText(spec.code.take(64), width / 2f, 680f, secondaryPaint)
        }
        barcode.bitmap.recycle()
        return result
    }
}

// بکاپ سبک محلی برای تاریخچه و تنظیمات نسخه 1.9.
object V19BackupManager {
    fun buildJson(history: List<HistoryItem>, settings: V19SettingsRepository): String {
        val root = JSONObject()
        root.put("schema", 1)
        root.put("settings", settings.exportSnapshot())
        val array = JSONArray()
        history.forEach { item ->
            array.put(
                JSONObject()
                    .put("kind", item.kind)
                    .put("payload", item.payload)
                    .put("createdAt", item.createdAt)
                    .put("favorite", item.favorite)
            )
        }
        root.put("history", array)
        return root.toString(2)
    }

    fun saveToCache(context: Context, json: String): File {
        val file = File(context.cacheDir, "QrCodeYar-backup-${System.currentTimeMillis()}.json")
        file.writeText(json, Charsets.UTF_8)
        return file
    }
}

// Folder و Tag به‌صورت مدل مستقل تعریف شده‌اند تا در نسخه بعدی مستقیماً به Room متصل شوند.
data class ArchiveFolder(val id: String, val title: String)
data class ArchiveTag(val id: String, val title: String)
