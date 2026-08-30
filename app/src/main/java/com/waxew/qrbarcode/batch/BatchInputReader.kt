/*
 * App-QrCodeYar v2.0 - ورود گروهی CSV/TXT/XLSX با انتخاب ستون.
 *
 * این Reader هم API قدیمی read() را حفظ می‌کند و هم readTable() را برای Mapping ستون‌ها
 * در اختیار UI می‌گذارد. حداکثر 500 ردیف خوانده می‌شود تا پردازش گروهی کنترل‌شده بماند.
 */
package com.waxew.qrbarcode.batch

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

data class BatchTable(
    val columns: List<String>,
    val rows: List<List<String>>
) {
    fun columnValues(index: Int): List<String> {
        if (index !in columns.indices) return emptyList()
        return rows.mapNotNull { row -> row.getOrNull(index)?.trim()?.takeIf { it.isNotBlank() } }
            .distinct()
    }
}

object BatchInputReader {
    private const val LIMIT = 500

    /** API سازگار با نسخه قبل: اولین ستون غیرخالی را برمی‌گرداند. */
    fun read(context: Context, uri: Uri): List<String> = readTable(context, uri).columnValues(0)

    /** فایل را به جدول چندستونه تبدیل می‌کند تا کاربر ستون Payload را انتخاب کند. */
    fun readTable(context: Context, uri: Uri): BatchTable {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return BatchTable(emptyList(), emptyList())
        val name = displayName(context, uri).lowercase()
        val isXlsx = name.endsWith(".xlsx") || (bytes.size > 4 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte())
        return if (isXlsx) readXlsxTable(bytes) else readDelimitedTable(bytes.toString(Charsets.UTF_8))
    }

    private fun readDelimitedTable(text: String): BatchTable {
        val rawLines = text.lineSequence()
            .map { it.removePrefix("\uFEFF").trimEnd() }
            .filter { it.isNotBlank() }
            .take(LIMIT)
            .toList()
        if (rawLines.isEmpty()) return BatchTable(emptyList(), emptyList())

        val delimiter = detectDelimiter(rawLines.first())
        val rows = rawLines.map { parseDelimitedLine(it, delimiter) }
        val width = rows.maxOfOrNull { it.size } ?: 1
        val normalized = rows.map { row -> List(width) { index -> row.getOrElse(index) { "" }.trim() } }
        return BatchTable(List(width) { "ستون ${it + 1}" }, normalized)
    }

    private fun detectDelimiter(line: String): Char = when {
        line.contains('\t') -> '\t'
        line.count { it == ';' } > line.count { it == ',' } -> ';'
        else -> ','
    }

    /** CSV parser کوچک با پشتیبانی از quoted cell و "" داخل متن. */
    private fun parseDelimitedLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var index = 0
        while (index < line.length) {
            val ch = line[index]
            when {
                ch == '"' && quoted && index + 1 < line.length && line[index + 1] == '"' -> {
                    cell.append('"')
                    index++
                }
                ch == '"' -> quoted = !quoted
                ch == delimiter && !quoted -> {
                    result += cell.toString()
                    cell.clear()
                }
                else -> cell.append(ch)
            }
            index++
        }
        result += cell.toString()
        return result
    }

    // XLSX یک ZIP از XML است؛ sharedStrings و sheet1 برای پردازش آفلاین خوانده می‌شوند.
    private fun readXlsxTable(bytes: ByteArray): BatchTable {
        val entries = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.name == "xl/sharedStrings.xml" || entry.name == "xl/worksheets/sheet1.xml") {
                    entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
                }
                zip.closeEntry()
            }
        }

        val sheet = entries["xl/worksheets/sheet1.xml"] ?: return BatchTable(emptyList(), emptyList())
        val shared = parseSharedStrings(entries["xl/sharedStrings.xml"].orEmpty())
        val rows = Regex("<row\\b[^>]*>(.*?)</row>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
            .findAll(sheet)
            .take(LIMIT)
            .map { rowMatch ->
                Regex("<c\\b([^>]*)>(.*?)</c>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
                    .findAll(rowMatch.groupValues[1])
                    .map { cellMatch -> decodeCell(cellMatch.groupValues[1], cellMatch.groupValues[2], shared).orEmpty().trim() }
                    .toList()
            }
            .filter { row -> row.any { it.isNotBlank() } }
            .toList()
        val width = rows.maxOfOrNull { it.size } ?: 0
        if (width == 0) return BatchTable(emptyList(), emptyList())
        val normalized = rows.map { row -> List(width) { index -> row.getOrElse(index) { "" } } }
        return BatchTable(List(width) { "ستون ${it + 1}" }, normalized)
    }

    private fun parseSharedStrings(xml: String): List<String> {
        if (xml.isBlank()) return emptyList()
        return Regex("<si\\b[^>]*>(.*?)</si>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
            .findAll(xml)
            .map { match ->
                Regex("<t\\b[^>]*>(.*?)</t>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
                    .findAll(match.groupValues[1])
                    .joinToString("") { unescapeXml(it.groupValues[1]) }
            }
            .toList()
    }

    private fun decodeCell(attributes: String, body: String, shared: List<String>): String? {
        val typeShared = Regex("\\bt=\"s\"", RegexOption.IGNORE_CASE).containsMatchIn(attributes)
        val inline = Regex("<t\\b[^>]*>(.*?)</t>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
            .find(body)?.groupValues?.getOrNull(1)
        if (inline != null) return unescapeXml(inline)
        val value = Regex("<v>(.*?)</v>", RegexOption.DOT_MATCHES_ALL).find(body)?.groupValues?.getOrNull(1) ?: return null
        return if (typeShared) shared.getOrNull(value.trim().toIntOrNull() ?: -1) else unescapeXml(value)
    }

    private fun displayName(context: Context, uri: Uri): String = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else ""
        }.orEmpty()
    }.getOrDefault("")

    private fun unescapeXml(value: String): String = value
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
}
