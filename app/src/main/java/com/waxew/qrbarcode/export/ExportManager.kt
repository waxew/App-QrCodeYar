package com.waxew.qrbarcode.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.zxing.common.BitMatrix
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ExportManager {
    fun savePng(context: Context, bitmap: Bitmap, displayName: String): Uri? {
        val name = "$displayName.png"
        return writeMedia(context, name, "image/png", MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "Pictures/QRStudio") { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    fun saveSvg(context: Context, matrix: BitMatrix, foreground: Int, background: Int, displayName: String): Uri? {
        val svg = buildSvg(matrix, foreground, background)
        return writeMedia(context, "$displayName.svg", "image/svg+xml", MediaStore.Downloads.EXTERNAL_CONTENT_URI, "Download/QRStudio") { out ->
            out.write(svg.toByteArray(Charsets.UTF_8))
        }
    }

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

        val uri = writeMedia(context, "$displayName.pdf", "application/pdf", MediaStore.Downloads.EXTERNAL_CONTENT_URI, "Download/QRStudio") { out ->
            document.writeTo(out)
        }
        document.close()
        return uri
    }

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
                resolver.openOutputStream(uri)?.use(writer) ?: return null
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

    private fun buildSvg(matrix: BitMatrix, foreground: Int, background: Int): String {
        val fg = colorHex(foreground)
        val bg = colorHex(background)
        val body = StringBuilder(matrix.width * matrix.height / 2)
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix[x, y]) body.append("<rect x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"1\" height=\"1\"/>")
            }
        }
        return """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${matrix.width} ${matrix.height}" shape-rendering="crispEdges"><rect width="100%" height="100%" fill="$bg"/><g fill="$fg">$body</g></svg>"""
    }

    private fun colorHex(color: Int): String = String.format("#%06X", 0xFFFFFF and color)
}
