/*
 * App-QrCodeYar v2.0 - موتور تولید QR و Barcode.
 *
 * ZXing ماتریس استاندارد را می‌سازد و این فایل فقط Render ظاهری را انجام می‌دهد. QR با
 * Error Correction سطح H ساخته می‌شود تا استفاده از لوگو/قاب تحمل بیشتری داشته باشد.
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

enum class ModuleStyle(val title: String, val premium: Boolean) {
    CLASSIC("کلاسیک", false), ROUNDED("گرد", true), DOTS("نقطه‌ای", true), BUBBLE("حبابی", true)
}

enum class FinderStyle(val title: String, val premium: Boolean) {
    CLASSIC("کلاسیک", false), ROUNDED("گوشه‌گرد", true), DOTS("نقطه‌ای", true)
}

enum class FrameStyle(val title: String, val premium: Boolean) {
    NONE("بدون قاب", false), ROUNDED("قاب گرد", true), LABEL("قاب + متن", true)
}

enum class GradientDirection(val title: String) {
    DIAGONAL("مورب"), HORIZONTAL("افقی"), VERTICAL("عمودی")
}

enum class LogoShape(val title: String) {
    ROUNDED("گردگوشه"), CIRCLE("دایره"), SQUARE("مربع")
}

data class QrDesign(
    val moduleStyle: ModuleStyle = ModuleStyle.CLASSIC,
    val finderStyle: FinderStyle = FinderStyle.CLASSIC,
    val foreground: Int = Color.rgb(42, 34, 55),
    val finderForeground: Int = foreground,
    val gradientEnd: Int = Color.rgb(129, 103, 180),
    val gradientEnabled: Boolean = false,
    val gradientDirection: GradientDirection = GradientDirection.DIAGONAL,
    val background: Int = Color.WHITE,
    val transparentBackground: Boolean = false,
    val backgroundImage: Bitmap? = null,
    val moduleScale: Float = 1f,
    val frameStyle: FrameStyle = FrameStyle.NONE,
    val frameText: String = "",
    val logo: Bitmap? = null,
    val logoShape: LogoShape = LogoShape.ROUNDED,
    val logoBorderColor: Int = Color.WHITE,
    val logoBorderWidth: Float = 0.06f
)

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

data class ReadabilityResult(val score: Int, val contrastRatio: Double, val message: String, val good: Boolean)

object CodeGenerator {
    fun qr(payload: String, size: Int = 768, design: QrDesign = QrDesign()): GeneratedCode {
        require(payload.isNotBlank()) { "محتوای QR نمی‌تواند خالی باشد." }
        require(design.moduleScale in 0.55f..1f) { "ضخامت ماژول خارج از محدوده مجاز است." }
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

    fun qr(
        payload: String,
        size: Int = 768,
        style: ModuleStyle = ModuleStyle.CLASSIC,
        foreground: Int = Color.rgb(42, 34, 55),
        background: Int = Color.WHITE
    ): GeneratedCode = qr(payload, size, QrDesign(moduleStyle = style, foreground = foreground, finderForeground = foreground, background = background))

    fun barcode(
        payload: String,
        format: BarcodeFormat,
        width: Int = 1280,
        height: Int = 480,
        foreground: Int = Color.rgb(42, 34, 55),
        background: Int = Color.WHITE
    ): GeneratedCode {
        require(payload.isNotBlank()) { "محتوای بارکد نمی‌تواند خالی باشد." }
        val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8", EncodeHintType.MARGIN to 16)
        val matrix = MultiFormatWriter().encode(payload, format, width, height, hints)
        return GeneratedCode(renderBarcode(matrix, foreground, background), matrix, foreground, background)
    }

    fun readability(design: QrDesign): ReadabilityResult {
        val bg = if (design.transparentBackground || design.backgroundImage != null) Color.WHITE else design.background
        val ratios = mutableListOf(contrastRatio(design.foreground, bg), contrastRatio(design.finderForeground, bg))
        if (design.gradientEnabled) ratios += contrastRatio(design.gradientEnd, bg)
        val ratio = ratios.minOrNull() ?: 1.0
        val logoPenalty = if (design.logo != null) 8 else 0
        val imagePenalty = if (design.backgroundImage != null) 8 else 0
        val scalePenalty = if (design.moduleScale < 0.75f) 12 else if (design.moduleScale < 0.9f) 5 else 0
        val baseScore = when {
            ratio >= 7.0 -> 100
            ratio >= 4.5 -> 90
            ratio >= 3.0 -> 72
            ratio >= 2.0 -> 50
            else -> 25
        }
        val score = (baseScore - logoPenalty - imagePenalty - scalePenalty).coerceIn(0, 100)
        val good = ratio >= 3.0 && score >= 60
        val message = when {
            ratio >= 7.0 && score >= 80 -> "کنتراست عالی؛ مناسب اسکن و چاپ."
            ratio >= 4.5 -> "کنتراست خوب؛ برای اکثر دوربین‌ها مناسب است."
            ratio >= 3.0 -> "قابل قبول است؛ قبل از چاپ حتماً اسکن آزمایشی بگیرید."
            else -> "کنتراست کم است؛ رنگ‌ها یا پس‌زمینه را اصلاح کنید."
        }
        return ReadabilityResult(score, ratio, message, good)
    }

    private fun renderQr(matrix: BitMatrix, design: QrDesign): Bitmap {
        val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        when {
            design.transparentBackground -> canvas.drawColor(Color.TRANSPARENT)
            design.backgroundImage != null -> {
                val image = design.backgroundImage
                val dest = RectF(0f, 0f, matrix.width.toFloat(), matrix.height.toFloat())
                canvas.drawBitmap(image, null, dest, Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 105 })
                canvas.drawColor(Color.argb(175, Color.red(design.background), Color.green(design.background), Color.blue(design.background)))
            }
            else -> canvas.drawColor(design.background)
        }

        val modulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = design.foreground
            if (design.gradientEnabled) {
                val (x1, y1) = when (design.gradientDirection) {
                    GradientDirection.DIAGONAL -> matrix.width.toFloat() to matrix.height.toFloat()
                    GradientDirection.HORIZONTAL -> matrix.width.toFloat() to 0f
                    GradientDirection.VERTICAL -> 0f to matrix.height.toFloat()
                }
                shader = LinearGradient(0f, 0f, x1, y1, design.foreground, design.gradientEnd, Shader.TileMode.CLAMP)
            }
        }
        val finderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = design.finderForeground }

        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (!matrix[x, y]) continue
                val finder = isFinderZone(x, y, matrix.width, matrix.height)
                if (finder) drawPixel(canvas, finderPaint, x, y, finderToModuleStyle(design.finderStyle), design.moduleScale)
                else drawPixel(canvas, modulePaint, x, y, design.moduleStyle, design.moduleScale)
            }
        }

        design.logo?.let { logo -> drawLogo(canvas, logo, matrix.width, matrix.height, design) }
        return bitmap
    }

    private fun drawLogo(canvas: Canvas, logo: Bitmap, width: Int, height: Int, design: QrDesign) {
        val side = (width * 0.18f).toInt().coerceAtLeast(48)
        val border = (side * design.logoBorderWidth.coerceIn(0f, 0.18f)).coerceAtLeast(2f)
        val boxSide = side + border * 2
        val left = (width - boxSide) / 2f
        val top = (height - boxSide) / 2f
        val box = RectF(left, top, left + boxSide, top + boxSide)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = design.logoBorderColor }
        when (design.logoShape) {
            LogoShape.CIRCLE -> canvas.drawOval(box, bgPaint)
            LogoShape.SQUARE -> canvas.drawRect(box, bgPaint)
            LogoShape.ROUNDED -> canvas.drawRoundRect(box, boxSide * 0.18f, boxSide * 0.18f, bgPaint)
        }

        val scaled = Bitmap.createScaledBitmap(logo, side, side, true)
        val logoLeft = (width - side) / 2f
        val logoTop = (height - side) / 2f
        val save = canvas.save()
        when (design.logoShape) {
            LogoShape.CIRCLE -> {
                val path = android.graphics.Path().apply { addCircle(width / 2f, height / 2f, side / 2f, android.graphics.Path.Direction.CW) }
                canvas.clipPath(path)
            }
            LogoShape.ROUNDED -> {
                val path = android.graphics.Path().apply {
                    addRoundRect(RectF(logoLeft, logoTop, logoLeft + side, logoTop + side), side * 0.12f, side * 0.12f, android.graphics.Path.Direction.CW)
                }
                canvas.clipPath(path)
            }
            LogoShape.SQUARE -> Unit
        }
        canvas.drawBitmap(scaled, logoLeft, logoTop, Paint(Paint.ANTI_ALIAS_FLAG))
        canvas.restoreToCount(save)
        if (scaled !== logo) scaled.recycle()
    }

    private fun renderBarcode(matrix: BitMatrix, foreground: Int, background: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(background)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = foreground }
        for (y in 0 until matrix.height) for (x in 0 until matrix.width) if (matrix[x, y]) {
            canvas.drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, paint)
        }
        return bitmap
    }

    private fun drawPixel(canvas: Canvas, paint: Paint, x: Int, y: Int, style: ModuleStyle, scale: Float) {
        val safeScale = scale.coerceIn(0.55f, 1f)
        val inset = (1f - safeScale) / 2f
        val left = x + inset
        val top = y + inset
        val right = x + 1f - inset
        val bottom = y + 1f - inset
        when (style) {
            ModuleStyle.CLASSIC -> canvas.drawRect(left, top, right, bottom, paint)
            ModuleStyle.DOTS -> canvas.drawCircle(x + 0.5f, y + 0.5f, 0.43f * safeScale, paint)
            ModuleStyle.ROUNDED -> canvas.drawRoundRect(RectF(left, top, right, bottom), 0.25f * safeScale, 0.25f * safeScale, paint)
            ModuleStyle.BUBBLE -> canvas.drawCircle(x + 0.5f, y + 0.5f, 0.49f * safeScale, paint)
        }
    }

    private fun finderToModuleStyle(style: FinderStyle): ModuleStyle = when (style) {
        FinderStyle.CLASSIC -> ModuleStyle.CLASSIC
        FinderStyle.ROUNDED -> ModuleStyle.ROUNDED
        FinderStyle.DOTS -> ModuleStyle.DOTS
    }

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
            color = design.finderForeground
            style = Paint.Style.STROKE
            strokeWidth = max(4f, source.width * 0.006f)
        }
        canvas.drawRoundRect(RectF(8f, 8f, width - 8f, height - 8f), width * 0.055f, width * 0.055f, borderPaint)
        canvas.drawBitmap(source, sidePadding.toFloat(), topPadding.toFloat(), null)
        if (design.frameStyle == FrameStyle.LABEL && design.frameText.isNotBlank()) {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = design.foreground
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = max(28f, source.width * 0.055f)
            }
            canvas.drawText(design.frameText.trim().take(42), width / 2f, source.height + topPadding + labelHeight * 0.64f, textPaint)
        }
        return result
    }

    private fun isFinderZone(x: Int, y: Int, width: Int, height: Int): Boolean {
        val zone = (width * 0.25f).toInt().coerceAtLeast(12)
        return (x < zone && y < zone) || (x >= width - zone && y < zone) || (x < zone && y >= height - zone)
    }

    private fun contrastRatio(first: Int, second: Int): Double {
        val l1 = relativeLuminance(first)
        val l2 = relativeLuminance(second)
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }

    private fun relativeLuminance(color: Int): Double {
        fun channel(value: Int): Double {
            val normalized = value / 255.0
            return if (normalized <= 0.03928) normalized / 12.92 else Math.pow((normalized + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(Color.red(color)) + 0.7152 * channel(Color.green(color)) + 0.0722 * channel(Color.blue(color))
    }
}
