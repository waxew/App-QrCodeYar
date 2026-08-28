/*
 * App-QrCodeYar - ابزار قالب‌بندی عدد
 *
 * قیمت‌های بدون جداکننده مانند 12000000 را به 12,000,000 تبدیل می‌کند. ارقام فارسی و عربی
 * نیز ابتدا به ارقام لاتین تبدیل می‌شوند تا نمایش قیمت در تمام فروشگاه‌ها یکدست باشد.
 */
package com.waxew.qrbarcode.util

object NumberFormatter {
    fun groupNumbersInText(value: String): String {
        val normalized = normalizeDigits(value)
        return Regex("\\d{4,}").replace(normalized) { match ->
            groupInteger(match.value)
        }
    }

    fun groupInteger(value: Long): String = groupInteger(value.toString())

    private fun groupInteger(raw: String): String {
        val negative = raw.startsWith('-')
        val digits = raw.removePrefix("-")
        val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
        return if (negative) "-$grouped" else grouped
    }

    private fun normalizeDigits(value: String): String {
        val persian = "۰۱۲۳۴۵۶۷۸۹"
        val arabic = "٠١٢٣٤٥٦٧٨٩"
        return buildString(value.length) {
            value.forEach { ch ->
                val p = persian.indexOf(ch)
                val a = arabic.indexOf(ch)
                when {
                    p >= 0 -> append(('0'.code + p).toChar())
                    a >= 0 -> append(('0'.code + a).toChar())
                    else -> append(ch)
                }
            }
        }
    }
}
