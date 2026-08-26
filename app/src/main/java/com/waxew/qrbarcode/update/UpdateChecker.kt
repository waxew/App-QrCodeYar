package com.waxew.qrbarcode.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.waxew.qrbarcode.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(val versionCode: Int, val versionName: String, val downloadUrl: String, val changelog: String)

object UpdateChecker {
    private const val URL_LATEST = "https://raw.githubusercontent.com/waxew/App-QrCodeYar/main/distribution/latest.json"

    fun check(): UpdateInfo? {
        return runCatching {
            val connection = URL(URL_LATEST).openConnection() as HttpURLConnection
            connection.connectTimeout = 3500
            connection.readTimeout = 3500
            connection.setRequestProperty("Accept", "application/json")
            val text = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            val json = JSONObject(text)
            val info = UpdateInfo(
                json.getInt("versionCode"),
                json.getString("versionName"),
                json.getString("downloadUrl"),
                json.optString("changelog")
            )
            if (info.versionCode > BuildConfig.VERSION_CODE) info else null
        }.getOrNull()
    }

    fun openDownload(context: Context, info: UpdateInfo) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl)))
    }
}
