/*
 * App-QrCodeYar - ورود گروهی داده برای QR
 *
 * کاربر می‌تواند CSV/TXT یا XLSX انتخاب کند. برای XLSX فقط Sheet اول و اولین سلول غیرخالی
 * هر ردیف خوانده می‌شود؛ این رفتار عمداً ساده است تا بدون کتابخانه سنگین Office روی Android
 * کار کند. حداکثر 100 ردیف برای جلوگیری از مصرف زیاد حافظه پردازش می‌شود.
 */
package com.waxew.qrbarcode.batch

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

object BatchInputReader {
    private const val LIMIT = 100

    fun read(context: Context, uri: Uri): List<String> {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return emptyList()
        val name = displayName(context, uri).lowercase()
        val isXlsx = name.endsWith(".xlsx") || (bytes.size > 4 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte())
        return if (isXlsx) readXlsx(bytes) else readDelimited(bytes.toString(Charsets.UTF_8))
    }

    // CSV/TSV/TXT: اولین ستون غیرخالی هر خط به‌عنوان payload در نظر گرفته می‌شود.
    private fun readDelimited(text: String): List<String> {
        return text
            .lineSequence()
            .map { it.removePrefix("\uFEFF").trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { line -> firstCell(line)?.trim()?.trim('"')?.takeIf { it.isNotBlank() } }
            .distinct()
            .take(LIMIT)
            .toList()
    }

    private fun firstCell(line: String): String? {
        val delimiter = when {
            line.contains('\t') -> '\t'
            line.contains(';') && !line.contains(',') -> ';'
            else -> ','
        }
        if (!line.startsWith('"')) return line.substringBefore(delimiter)

        // parser کوچک برای سلول quoted؛ "" داخل CSV به " تبدیل می‌شود.
        val out = StringBuilder()
        var index = 1
        while (index < line.length) {
            val ch = line[index]
            if (ch == '"') {
                if (index + 1 < line.length && line[index + 1] == '"') {
                    out.append('"')
                    index += 2
                    continue
                }
                break
            }
            out.append(ch)
            index++
        }
        return out.toString()
    }

    // XLSX در اصل ZIP شامل XML است. فقط sharedStrings و sheet1 برای نیاز این برنامه خوانده می‌شوند.
    private fun readXlsx(bytes: ByteArray): List<String> {
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

        val sheet = entries["xl/worksheets/sheet1.xml"] ?: return emptyList()
        val shared = parseSharedStrings(entries["xl/sharedStrings.xml"].orEmpty())
        val rows = Regex("<row\\b[^>]*>(.*?)</row>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
            .findAll(sheet)

        return rows.mapNotNull { rowMatch ->
            val rowBody = rowMatch.groupValues[1]
            val cell = Regex("<c\\b([^>]*)>(.*?)</c>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
                .findAll(rowBody)
                .mapNotNull { cellMatch -> decodeCell(cellMatch.groupValues[1], cellMatch.groupValues[2], shared) }
                .firstOrNull { it.isNotBlank() }
            cell?.trim()
        }.filter { it.isNotBlank() }.distinct().take(LIMIT).toList()
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

    private fun displayName(context: Context, uri: Uri): String {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else ""
            }.orEmpty()
        }.getOrDefault("")
    }

    private fun unescapeXml(value: String): String = value
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
}
