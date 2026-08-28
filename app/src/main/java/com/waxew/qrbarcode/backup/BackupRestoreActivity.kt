/*
 * Restore بکاپ از File Manager.
 *
 * این Activity فقط ACTION_VIEW با JSON را می‌پذیرد. فایل قبل از Restore از نظر اندازه، schema
 * و ساختار داده توسط V19BackupManager بررسی می‌شود. PIN از بکاپ Restore نمی‌شود.
 */
package com.waxew.qrbarcode.backup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.waxew.qrbarcode.data.PreferencesRepository
import com.waxew.qrbarcode.ui.theme.QrStudioTheme
import com.waxew.qrbarcode.v19.V19BackupManager
import com.waxew.qrbarcode.v19.V19SettingsRepository

class BackupRestoreActivity : ComponentActivity() {
    private lateinit var preferences: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferencesRepository(this)
        val inputUri = intent?.data

        setContent {
            QrStudioTheme {
                var message by remember { mutableStateOf("آماده بررسی فایل بکاپ") }
                var finished by remember { mutableStateOf(false) }

                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("بازیابی بکاپ QR یار", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        Text(message, textAlign = TextAlign.Center)

                        if (!finished) {
                            Button(onClick = {
                                val result = runCatching {
                                    requireNotNull(inputUri) { "فایل بکاپ مشخص نیست." }
                                    val json = contentResolver.openInputStream(inputUri)
                                        ?.bufferedReader(Charsets.UTF_8)
                                        ?.use { it.readText() }
                                        ?: error("فایل قابل خواندن نیست.")

                                    val payload = V19BackupManager.parseJson(json)
                                    val settings = V19SettingsRepository(this@BackupRestoreActivity)
                                    settings.importSnapshot(payload.settingsSnapshot)
                                    preferences.restoreHistory(payload.history)
                                    "بکاپ پذیرفته شد و بازیابی در حال ثبت نهایی است. پس از بستن، برنامه را دوباره باز کنید."
                                }
                                message = result.getOrElse { "بازیابی انجام نشد: ${it.message ?: "فایل نامعتبر"}" }
                                finished = true
                            }) { Text("بررسی و بازیابی") }
                        } else {
                            Button(onClick = { finish() }) { Text("بستن") }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (::preferences.isInitialized) preferences.close()
        super.onDestroy()
    }
}
