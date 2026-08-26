/*
 * سرویس ساده بررسی بروزرسانی.
 * فایل distribution/latest.json در GitHub منبع نسخه جدید است.
 */
package com.waxew.qrbarcode.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.waxew.qrbarcode.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// داده‌ای که از latest.json خوانده می‌شود.
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val changelog: String
)

object UpdateChecker {
    // ریپوی اصلی برنامه؛ تغییر ریپو باید فقط از همین ثابت و latest.json انجام شود.
    private const val URL_LATEST =
        "https://raw.githubusercontent.com/waxew/App-QrCodeYar/main/distribution/latest.json"

    fun check(): UpdateInfo? {
        return runCatching {
            // اتصال کوتاه‌مدت HTTP؛ نبود اینترنت نباید باعث کرش برنامه شود.
            val connection = URL(URL_LATEST).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 3500
                connection.readTimeout = 3500
                connection.setRequestProperty("Accept", "application/json")

                // فقط پاسخ‌های موفق 2xx پردازش می‌شوند.
                if (connection.responseCode !in 200..299) return@runCatching null

                val text = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(text)
                val info = UpdateInfo(
                    versionCode = json.getInt("versionCode"),
                    versionName = json.getString("versionName"),
                    downloadUrl = json.getString("downloadUrl"),
                    changelog = json.optString("changelog")
                )

                // اگر نسخه GitHub جدیدتر از Build فعلی نباشد، دیالوگ نمایش داده نمی‌شود.
                if (info.versionCode > BuildConfig.VERSION_CODE) info else null
            } finally {
                // حتی در صورت خطا اتصال آزاد می‌شود.
                connection.disconnect()
            }
        }.getOrNull()
    }

    fun openDownload(context: Context, info: UpdateInfo) {
        // لینک دانلود توسط مرورگر یا اپ مناسب سیستم باز می‌شود.
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl)))
    }
}
