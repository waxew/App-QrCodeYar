/*
 * موتور تولید QR و Barcode بر پایه ZXing.
 * Finder patternهای QR عمداً کلاسیک نگه داشته می‌شوند تا استایل‌های فانتزی قابلیت اسکن را خراب نکنند.
 */
package com.waxew.qrbarcode.generator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.common.BitMatrix

// استایل‌های CLASSIC رایگان و سایر استایل‌ها Pro هستند.
enum class ModuleStyle(val title: String, val premium: Boolean) {
    CLASSIC("کلاسیک", false),
    ROUNDED("گرد", true),
    DOTS("نقطه‌ای", true),
    BUBBLE("حبابی", true)
}

// نتیجه شامل Bitmap برای نمایش/PNG و BitMatrix برای SVG است.
data class GeneratedCode(
    val bitmap: Bitmap,
    val matrix: BitMatrix,
    val foreground: Int,
    val background: Int
)

object CodeGenerator {
    // تولید QR با UTF-8، تصحیح خطای H و حاشیه امن.
    fun qr(
        payload: String,
        size: Int = 768,
        style: ModuleStyle = ModuleStyle.CLASSIC,
        foreground: Int = android.graphics.Color.rgb(42, 34, 55),
        background: Int = android.graphics.Color.WHITE
    ): GeneratedCode {
        require(payload.isNotBlank()) { "محتوای QR نمی‌تواند خالی باشد." }
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 2
        )
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size, hints)
        return GeneratedCode(render(matrix, style, foreground, background), matrix, foreground, background)
    }

    // تولید انواع بارکدهای پشتیبانی‌شده ZXing.
    fun barcode(
        payload: String,
        format: BarcodeFormat,
        width: Int = 1280,
        height: Int = 480,
        foreground: Int = android.graphics.Color.rgb(42, 34, 55),
        background: Int = android.graphics.Color.WHITE
    ): GeneratedCode {
        require(payload.isNotBlank()) { "محتوای بارکد نمی‌تواند خالی باشد." }
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 16
        )
        val matrix = MultiFormatWriter().encode(payload, format, width, height, hints)
        return GeneratedCode(render(matrix, ModuleStyle.CLASSIC, foreground, background), matrix, foreground, background)
    }

    // BitMatrix را با توجه به استایل انتخاب‌شده روی Bitmap نقاشی می‌کند.
    private fun render(matrix: BitMatrix, style: ModuleStyle, foreground: Int, background: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(background)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = foreground }

        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (!matrix[x, y]) continue

                val finder = isFinderZone(x, y, matrix.width, matrix.height)
                when {
                    finder || style == ModuleStyle.CLASSIC -> {
                        canvas.drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, paint)
                    }
                    style == ModuleStyle.DOTS -> {
                        canvas.drawCircle(x + 0.5f, y + 0.5f, 0.43f, paint)
                    }
                    style == ModuleStyle.ROUNDED -> {
                        canvas.drawRoundRect(RectF(x + 0.08f, y + 0.08f, x + 0.92f, y + 0.92f), 0.25f, 0.25f, paint)
                    }
                    style == ModuleStyle.BUBBLE -> {
                        canvas.drawCircle(x + 0.5f, y + 0.5f, 0.49f, paint)
                    }
                }
            }
        }
        return bitmap
    }

    // سه گوشه تشخیص QR کلاسیک باقی می‌مانند تا اسکن پایدار بماند.
    private fun isFinderZone(x: Int, y: Int, width: Int, height: Int): Boolean {
        val zone = (width * 0.25f).toInt().coerceAtLeast(12)
        val topLeft = x < zone && y < zone
        val topRight = x >= width - zone && y < zone
        val bottomLeft = x < zone && y >= height - zone
        return topLeft || topRight || bottomLeft
    }
}
