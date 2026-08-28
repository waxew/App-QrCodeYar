/*
 * App-QrCodeYar - خواندن QR/Barcode از تصویر و تحلیل ایمنی لینک
 *
 * این فایل برای اسکن عکس‌های Gallery از ZXing Core استفاده می‌کند و به دوربین وابسته نیست.
 * GenericMultipleBarcodeReader اجازه می‌دهد در یک تصویر چند کد پیدا شود. تحلیل لینک نیز کاملاً
 * محلی است و فقط نشانه‌های رایج ریسک را هشدار می‌دهد؛ هیچ URL برای بررسی به سرور ارسال نمی‌شود.
 */
package com.waxew.qrbarcode.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.GenericMultipleBarcodeReader
import java.net.IDN
import java.util.EnumMap

// خروجی یک کد خوانده‌شده از تصویر.
data class DecodedImageCode(
    val text: String,
    val format: String
)

enum class LinkRiskLevel { SAFE, CAUTION }

data class LinkSafetyResult(
    val isUrl: Boolean,
    val level: LinkRiskLevel,
    val message: String
)

object ImageCodeDecoder {
    private const val MAX_IMAGE_SIDE = 2200

    // تصویر URI را با اندازه کنترل‌شده Decode می‌کند تا عکس‌های بزرگ باعث OutOfMemory نشوند.
    fun decodeAll(context: Context, uri: Uri): List<DecodedImageCode> {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: return emptyList()

        val prepared = scaleDown(bitmap, MAX_IMAGE_SIDE)
        if (prepared !== bitmap) bitmap.recycle()

        return try {
            decodeBitmap(prepared)
        } finally {
            prepared.recycle()
        }
    }

    private fun decodeBitmap(bitmap: Bitmap): List<DecodedImageCode> {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return emptyList()

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)
        val binary = BinaryBitmap(HybridBinarizer(source))

        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(DecodeHintType.TRY_HARDER, true)
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
        }
        val reader = MultiFormatReader().apply { setHints(hints) }
        val multi = GenericMultipleBarcodeReader(reader)

        val results: Array<Result> = runCatching { multi.decodeMultiple(binary, hints) }
            .getOrElse {
                runCatching { arrayOf(reader.decode(binary, hints)) }.getOrDefault(emptyArray())
            }

        return results
            .map { result -> DecodedImageCode(result.text.orEmpty(), result.barcodeFormat?.name ?: "UNKNOWN") }
            .filter { it.text.isNotBlank() }
            .distinctBy { "${it.format}:${it.text}" }
    }

    private fun scaleDown(source: Bitmap, maxSide: Int): Bitmap {
        val largest = maxOf(source.width, source.height)
        if (largest <= maxSide) return source
        val ratio = maxSide.toFloat() / largest.toFloat()
        val width = (source.width * ratio).toInt().coerceAtLeast(1)
        val height = (source.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, width, height, true)
    }
}

object ScanSafetyAnalyzer {
    private val shortenerHosts = setOf(
        "bit.ly", "tinyurl.com", "t.co", "goo.gl", "is.gd", "cutt.ly", "rebrand.ly"
    )

    // تحلیل ساده آفلاین برای کاهش بازکردن ناخواسته لینک‌های مشکوک.
    fun analyze(text: String): LinkSafetyResult {
        val trimmed = text.trim()
        val lower = trimmed.lowercase()
        val isUrl = lower.startsWith("http://") || lower.startsWith("https://")
        if (!isUrl) return LinkSafetyResult(false, LinkRiskLevel.SAFE, "محتوا لینک وب نیست.")

        val uri = runCatching { Uri.parse(trimmed) }.getOrNull()
            ?: return LinkSafetyResult(true, LinkRiskLevel.CAUTION, "ساختار لینک قابل اعتماد نیست.")
        val host = uri.host?.lowercase().orEmpty()
        val reasons = mutableListOf<String>()

        if (uri.scheme.equals("http", ignoreCase = true)) reasons += "اتصال HTTPS ندارد"
        if (host.startsWith("xn--") || host.split('.').any { it.startsWith("xn--") }) reasons += "دامنه Punycode دارد"
        if (isIpHost(host)) reasons += "به‌جای دامنه از IP استفاده می‌کند"
        if (host in shortenerHosts) reasons += "لینک کوتاه‌شده مقصد واقعی را پنهان می‌کند"
        if (trimmed.count { it == '@' } > 0) reasons += "کاراکتر @ داخل لینک دیده شد"
        if (host.length > 70) reasons += "نام دامنه غیرعادی طولانی است"

        runCatching { if (host.isNotBlank()) IDN.toASCII(host) }.onFailure { reasons += "نام دامنه نامعتبر است" }

        return if (reasons.isEmpty()) {
            LinkSafetyResult(true, LinkRiskLevel.SAFE, "نشانه واضحی از ریسک در ساختار لینک دیده نشد؛ مقصد را قبل از ورود بررسی کنید.")
        } else {
            LinkSafetyResult(true, LinkRiskLevel.CAUTION, "احتیاط: ${reasons.joinToString("، ")}.")
        }
    }

    private fun isIpHost(host: String): Boolean {
        if (host.isBlank()) return false
        val ipv4 = Regex("^(?:\\d{1,3}\\.){3}\\d{1,3}$")
        val ipv6 = host.contains(':')
        return ipv4.matches(host) || ipv6
    }
}
