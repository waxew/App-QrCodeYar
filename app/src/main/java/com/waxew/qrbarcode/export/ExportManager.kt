/*
 * App-QrCodeYar - مدیریت خروجی فایل‌ها
 *
 * PNG در Pictures/QRStudio و PDF/SVG در Downloads/QRStudio ذخیره می‌شوند. در Android 10+
 * از Scoped Storage/MediaStore استفاده می‌کنیم و در نسخه‌های قدیمی‌تر پوشه اختصاصی برنامه
 * استفاده می‌شود. نسخه 1.1 خروجی A4 چندلیبلی برای تولید گروهی QR را نیز اضافه می‌کند.
 */
package com.waxew.qrbarcode.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.zxing.common.BitMatrix
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ExportManager {
    // خروجی PNG استاندارد با فشرده‌سازی بدون اتلاف.
    fun savePng(context: Context, bitmap: Bitmap, displayName: String): Uri? {
        val name = "$displayName.png"
        return writeMedia(
            context,
            name,
            "image/png",
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "Pictures/QRStudio"
        ) { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    // خروجی SVG از BitMatrix استاندارد. استایل‌های Bitmap مثل لوگو/قاب در SVG کلاسیک وارد نمی‌شوند.
    fun saveSvg(context: Context, matrix: BitMatrix, foreground: Int, background: Int, displayName: String): Uri? {
        val svg = buildSvg(matrix, foreground, background)
        return writeMedia(
            context,
            "$displayName.svg",
            "image/svg+xml",
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            "Download/QRStudio"
        ) { out ->
            out.write(svg.toByteArray(Charsets.UTF_8))
        }
    }

    // خروجی PDF تک‌کد روی صفحه A4 مجازی.
    fun savePdf(context: Context, bitmap: Bitmap, displayName: String): Uri? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(1240, 1754, 1).create()
        val page = document.startPage(pageInfo)
        val maxWidth = 1000f
        val maxHeight = 1200f
        val scale = minOf(maxWidth / bitmap.width, maxHeight / bitmap.height, 1f)
        val left = (pageInfo.pageWidth - bitmap.width * scale) / 2f
        val top = (pageInfo.pageHeight - bitmap.height * scale) / 2f
        page.canvas.save()
        page.canvas.translate(left, top)
        page.canvas.scale(scale, scale)
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        page.canvas.restore()
        document.finishPage(page)

        val uri = writeMedia(
            context,
            "$displayName.pdf",
            "application/pdf",
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            "Download/QRStudio"
        ) { out ->
            document.writeTo(out)
        }
        document.close()
        return uri
    }

    // خروجی چند QR روی A4؛ هر صفحه 3 ستون × 5 ردیف دارد و برای چاپ لیبل مناسب است.
    fun saveA4LabelPdf(context: Context, bitmaps: List<Bitmap>, displayName: String): Uri? {
        require(bitmaps.isNotEmpty()) { "حداقل یک QR برای ساخت صفحه لیبل لازم است." }
        val document = PdfDocument()
        val pageWidth = 1240
        val pageHeight = 1754
        val columns = 3
        val rows = 5
        val perPage = columns * rows
        val margin = 48f
        val gap = 18f
        val cellWidth = (pageWidth - margin * 2 - gap * (columns - 1)) / columns
        val cellHeight = (pageHeight - margin * 2 - gap * (rows - 1)) / rows
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = android.graphics.Color.LTGRAY
        }

        bitmaps.chunked(perPage).forEachIndexed { pageIndex, pageItems ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
            val page = document.startPage(pageInfo)
            pageItems.forEachIndexed { index, bitmap ->
                val row = index / columns
                val column = index % columns
                val left = margin + column * (cellWidth + gap)
                val top = margin + row * (cellHeight + gap)
                val cell = RectF(left, top, left + cellWidth, top + cellHeight)
                page.canvas.drawRoundRect(cell, 12f, 12f, borderPaint)

                val innerPadding = 18f
                val availableW = cellWidth - innerPadding * 2
                val availableH = cellHeight - innerPadding * 2
                val scale = minOf(availableW / bitmap.width, availableH / bitmap.height)
                val drawW = bitmap.width * scale
                val drawH = bitmap.height * scale
                val dest = RectF(
                    left + (cellWidth - drawW) / 2f,
                    top + (cellHeight - drawH) / 2f,
                    left + (cellWidth + drawW) / 2f,
                    top + (cellHeight + drawH) / 2f
                )
                page.canvas.drawBitmap(bitmap, null, dest, null)
            }
            document.finishPage(page)
        }

        val uri = writeMedia(
            context,
            "$displayName.pdf",
            "application/pdf",
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            "Download/QRStudio"
        ) { out ->
            document.writeTo(out)
        }
        document.close()
        return uri
    }

    // نقطه مشترک تمام عملیات ذخیره‌سازی؛ در خطا رکورد ناقص MediaStore پاک می‌شود.
    private fun writeMedia(
        context: Context,
        name: String,
        mime: String,
        collection: Uri,
        relativePath: String,
        writer: (OutputStream) -> Unit
    ): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(collection, values) ?: return null
            try {
                val stream = resolver.openOutputStream(uri)
                if (stream == null) {
                    resolver.delete(uri, null, null)
                    return null
                }
                stream.use(writer)
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                uri
            } catch (t: Throwable) {
                resolver.delete(uri, null, null)
                throw t
            }
        } else {
            val dir = File(context.getExternalFilesDir(null), "QRStudio").apply { mkdirs() }
            val file = File(dir, name)
            FileOutputStream(file).use(writer)
            Uri.fromFile(file)
        }
    }

    // تبدیل BitMatrix به SVG کلاسیک و سازگار با چاپ.
    private fun buildSvg(matrix: BitMatrix, foreground: Int, background: Int): String {
        val fg = colorHex(foreground)
        val bg = if (background == android.graphics.Color.TRANSPARENT) "none" else colorHex(background)
        val body = StringBuilder(matrix.width * matrix.height / 2)
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix[x, y]) {
                    body.append("<rect x=\"").append(x)
                        .append("\" y=\"").append(y)
                        .append("\" width=\"1\" height=\"1\"/>")
                }
            }
        }
        return """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${matrix.width} ${matrix.height}" shape-rendering="crispEdges"><rect width="100%" height="100%" fill="$bg"/><g fill="$fg">$body</g></svg>"""
    }

    private fun colorHex(color: Int): String = String.format("#%06X", 0xFFFFFF and color)
}
