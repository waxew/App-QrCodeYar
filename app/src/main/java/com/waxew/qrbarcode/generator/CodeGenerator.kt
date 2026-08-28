/*
 * App-QrCodeYar - موتور تولید QR و Barcode
 *
 * ZXing ماتریس استاندارد کد را می‌سازد و این فایل مسئول رندر ظاهری آن است. در نسخه 1.1
 * امکانات استودیویی مثل گرادیان، استایل جداگانه Finder، لوگو و قاب اضافه شده‌اند. سطح
 * تصحیح خطای QR روی H نگه داشته می‌شود تا QRهای دارای لوگو تحمل بیشتری داشته باشند.
 */
package com.waxew.qrbarcode.generator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.math.max

// استایل CLASSIC رایگان است و سایر مدل‌ها برای طراحی Pro استفاده می‌شوند.
enum class ModuleStyle(val title: String, val premium: Boolean) {
    CLASSIC("کلاسیک", false),
    ROUNDED("گرد", true),
    DOTS("نقطه‌ای", true),
    BUBBLE("حبابی", true)
}

// Finder همان سه علامت بزرگ گوشه QR است و مستقل از ماژول‌های میانی قابل تغییر است.
enum class FinderStyle(val title: String, val premium: Boolean) {
    CLASSIC("کلاسیک", false),
    ROUNDED("گوشه‌گرد", true),
    DOTS("نقطه‌ای", true)
}

// قاب فقط روی Bitmap خروجی اثر می‌گذارد و داده QR را تغییر نمی‌دهد.
enum class FrameStyle(val title: String, val premium: Boolean) {
    NONE("بدون قاب", false),
    ROUNDED("قاب گرد", true),
    LABEL("قاب + متن", true)
}

// همه گزینه‌های ظاهری QR در یک مدل قرار گرفته‌اند تا Undo/Redo در UI ساده باشد.
data class QrDesign(
    val moduleStyle: ModuleStyle = ModuleStyle.CLASSIC,
    val finderStyle: FinderStyle = FinderStyle.CLASSIC,
    val foreground: Int = Color.rgb(42, 34, 55),
    val gradientEnd: Int = Color.rgb(129, 103, 180),
    val gradientEnabled: Boolean = false,
    val background: Int = Color.WHITE,
    val transparentBackground: Boolean = false,
    val frameStyle: FrameStyle = FrameStyle.NONE,
    val frameText: String = "",
    val logo: Bitmap? = null
)

// نتیجه تولید شامل Bitmap برای PNG/PDF و BitMatrix برای SVG/پردازش برداری است.
data class GeneratedCode(
    val bitmap: Bitmap,
    val matrix: BitMatrix,
    val foreground: Int,
    val background: Int,
    val gradientEnd: Int? = null,
    val transparentBackground: Boolean = false,
    val hasLogo: Boolean = false,
    val frameStyle: FrameStyle = FrameStyle.NONE
)

data class ReadabilityResult(
    val score: Int,
    val contrastRatio: Double,
    val message: String,
    val good: Boolean
)

object CodeGenerator {
    // تولید QR با UTF-8، Error Correction سطح H و حاشیه امن.
    fun qr(
        payload: String,
        size: Int = 768,
        design: QrDesign = QrDesign()
    ): GeneratedCode {
        require(payload.isNotBlank()) { "محتوای QR نمی‌تواند خالی باشد." }
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 2
        )
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size, hints)
        val qrBitmap = renderQr(matrix, design)
        val framed = applyFrame(qrBitmap, design)
        return GeneratedCode(
            bitmap = framed,
            matrix = matrix,
            foreground = design.foreground,
            background = if (design.transparentBackground) Color.TRANSPARENT else design.background,
            gradientEnd = design.gradientEnd.takeIf { design.gradientEnabled },
            transparentBackground = design.transparentBackground,
            hasLogo = design.logo != null,
            frameStyle = design.frameStyle
        )
    }

    // Overload سازگار با نسخه قبلی؛ کدهای قدیمی بدون تغییر کامپایل می‌شوند.
    fun qr(
        payload: String,
        size: Int = 768,
        style: ModuleStyle = ModuleStyle.CLASSIC,
        foreground: Int = Color.rgb(42, 34, 55),
        background: Int = Color.WHITE
    ): GeneratedCode = qr(
        payload = payload,
        size = size,
        design = QrDesign(moduleStyle = style, foreground = foreground, background = background)
    )

    // تولید انواع Barcode پشتیبانی‌شده توسط ZXing.
    fun barcode(
        payload: String,
        format: BarcodeFormat,
        width: Int = 1280,
        height: Int = 480,
        foreground: Int = Color.rgb(42, 34, 55),
        background: Int = Color.WHITE
    ): GeneratedCode {
        require(payload.isNotBlank()) { "محتوای بارکد نمی‌تواند خالی باشد." }
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 16
        )
        val matrix = MultiFormatWriter().encode(payload, format, width, height, hints)
        return GeneratedCode(
            bitmap = renderBarcode(matrix, foreground, background),
            matrix = matrix,
            foreground = foreground,
            background = background
        )
    }

    // تخمین خوانایی بر پایه Contrast Ratio. برای گرادیان، ضعیف‌ترین انتها ملاک است.
    fun readability(design: QrDesign): ReadabilityResult {
        val bg = if (design.transparentBackground) Color.WHITE else design.background
        val first = contrastRatio(design.foreground, bg)
        val second = if (design.gradientEnabled) contrastRatio(design.gradientEnd, bg) else first
        val ratio = minOf(first, second)
        val logoPenalty = if (design.logo != null) 8 else 0
        val baseScore = when {
            ratio >= 7.0 -> 100
            ratio >= 4.5 -> 90
            ratio >= 3.0 -> 72
            ratio >= 2.0 -> 50
            else -> 25
        }
        val score = (baseScore - logoPenalty).coerceIn(0, 100)
        val good = ratio >= 3.0 && score >= 60
        val message = when {
            ratio >= 7.0 -> "کنتراست عالی؛ مناسب اسکن و چاپ."
            ratio >= 4.5 -> "کنتراست خوب؛ برای اکثر دوربین‌ها مناسب است."
            ratio >= 3.0 -> "قابل قبول است؛ قبل از چاپ حتماً اسکن آزمایشی بگیرید."
            else -> "کنتراست کم است؛ رنگ‌ها را تیره‌تر/روشن‌تر کنید."
        }
        return ReadabilityResult(score, ratio, message, good)
    }

    private fun renderQr(matrix: BitMatrix, design: QrDesign): Bitmap {
        val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (design.transparentBackground) canvas.drawColor(Color.TRANSPARENT) else canvas.drawColor(design.background)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = design.foreground
            if (design.gradientEnabled) {
                shader = LinearGradient(
                    0f,
                    0f,
                    matrix.width.toFloat(),
                    matrix.height.toFloat(),
                    design.foreground,
                    design.gradientEnd,
                    Shader.TileMode.CLAMP
                )
            }
        }

        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (!matrix[x, y]) continue
                val finder = isFinderZone(x, y, matrix.width, matrix.height)
                if (finder) drawPixel(canvas, paint, x, y, finderToModuleStyle(design.finderStyle))
                else drawPixel(canvas, paint, x, y, design.moduleStyle)
            }
        }

        // لوگو حداکثر 18 درصد عرض QR را می‌گیرد تا بخش زیادی از داده پوشانده نشود.
        design.logo?.let { logo ->
            val side = (matrix.width * 0.18f).toInt().coerceAtLeast(48)
            val boxSide = (side * 1.22f).toInt()
            val left = (matrix.width - boxSide) / 2f
            val top = (matrix.height - boxSide) / 2f
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
            canvas.drawRoundRect(
                RectF(left, top, left + boxSide, top + boxSide),
                boxSide * 0.18f,
                boxSide * 0.18f,
                bgPaint
            )
            val scaled = Bitmap.createScaledBitmap(logo, side, side, true)
            val logoLeft = (matrix.width - side) / 2f
            val logoTop = (matrix.height - side) / 2f
            canvas.drawBitmap(scaled, logoLeft, logoTop, Paint(Paint.ANTI_ALIAS_FLAG))
            if (scaled !== logo) scaled.recycle()
        }
        return bitmap
    }

    private fun renderBarcode(matrix: BitMatrix, foreground: Int, background: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(background)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = foreground }
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix[x, y]) canvas.drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, paint)
            }
        }
        return bitmap
    }

    private fun drawPixel(canvas: Canvas, paint: Paint, x: Int, y: Int, style: ModuleStyle) {
        when (style) {
            ModuleStyle.CLASSIC -> canvas.drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, paint)
            ModuleStyle.DOTS -> canvas.drawCircle(x + 0.5f, y + 0.5f, 0.43f, paint)
            ModuleStyle.ROUNDED -> canvas.drawRoundRect(
                RectF(x + 0.08f, y + 0.08f, x + 0.92f, y + 0.92f),
                0.25f,
                0.25f,
                paint
            )
            ModuleStyle.BUBBLE -> canvas.drawCircle(x + 0.5f, y + 0.5f, 0.49f, paint)
        }
    }

    private fun finderToModuleStyle(style: FinderStyle): ModuleStyle = when (style) {
        FinderStyle.CLASSIC -> ModuleStyle.CLASSIC
        FinderStyle.ROUNDED -> ModuleStyle.ROUNDED
        FinderStyle.DOTS -> ModuleStyle.DOTS
    }

    // قاب خارج از خود QR اضافه می‌شود و بنابراین BitMatrix استاندارد دست‌نخورده می‌ماند.
    private fun applyFrame(source: Bitmap, design: QrDesign): Bitmap {
        if (design.frameStyle == FrameStyle.NONE) return source
        val sidePadding = max(28, (source.width * 0.055f).toInt())
        val topPadding = sidePadding
        val labelHeight = if (design.frameStyle == FrameStyle.LABEL) max(90, (source.height * 0.15f).toInt()) else sidePadding
        val width = source.width + sidePadding * 2
        val height = source.height + topPadding + labelHeight
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val frameBackground = if (design.transparentBackground) Color.WHITE else design.background
        canvas.drawColor(frameBackground)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = design.foreground
            style = Paint.Style.STROKE
            strokeWidth = max(4f, source.width * 0.006f)
        }
        canvas.drawRoundRect(
            RectF(8f, 8f, width - 8f, height - 8f),
            width * 0.055f,
            width * 0.055f,
            borderPaint
        )
        canvas.drawBitmap(source, sidePadding.toFloat(), topPadding.toFloat(), null)

        if (design.frameStyle == FrameStyle.LABEL && design.frameText.isNotBlank()) {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = design.foreground
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = max(28f, source.width * 0.055f)
            }
            val maxText = design.frameText.trim().take(42)
            canvas.drawText(maxText, width / 2f, source.height + topPadding + labelHeight * 0.64f, textPaint)
        }
        return result
    }

    private fun isFinderZone(x: Int, y: Int, width: Int, height: Int): Boolean {
        val zone = (width * 0.25f).toInt().coerceAtLeast(12)
        val topLeft = x < zone && y < zone
        val topRight = x >= width - zone && y < zone
        val bottomLeft = x < zone && y >= height - zone
        return topLeft || topRight || bottomLeft
    }

    private fun contrastRatio(first: Int, second: Int): Double {
        val l1 = relativeLuminance(first)
        val l2 = relativeLuminance(second)
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Int): Double {
        fun channel(value: Int): Double {
            val normalized = value / 255.0
            return if (normalized <= 0.03928) normalized / 12.92 else Math.pow((normalized + 0.055) / 1.055, 2.4)
        }
        val red = channel(Color.red(color))
        val green = channel(Color.green(color))
        val blue = channel(Color.blue(color))
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue
    }
}
