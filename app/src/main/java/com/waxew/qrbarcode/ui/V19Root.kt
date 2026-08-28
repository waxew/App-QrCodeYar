/*
 * App-QrCodeYar v1.9 - لایه یکپارچه قابلیت‌های جدید
 *
 * برنامه اصلی بدون شکستن ناوبری نسخه 1.1 حفظ می‌شود و مرکز ابزار 1.9 به‌صورت Overlay
 * روی همان ریشه قرار می‌گیرد. این روش ریسک Regression را کاهش می‌دهد و قابلیت‌های جدید را
 * برای کاربر قابل دسترس می‌کند.
 */
package com.waxew.qrbarcode.ui

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.waxew.qrbarcode.billing.BillingManager
import com.waxew.qrbarcode.data.PreferencesRepository
import com.waxew.qrbarcode.v19.ProductLabelRenderer
import com.waxew.qrbarcode.v19.ProductLabelSpec
import com.waxew.qrbarcode.v19.SmartTemplateCatalog
import com.waxew.qrbarcode.v19.V19AppLock
import com.waxew.qrbarcode.v19.V19BackupManager
import com.waxew.qrbarcode.v19.V19LinkSecurity
import com.waxew.qrbarcode.v19.V19SettingsRepository

@Composable
fun QrCodeYarV19Root(
    activity: Activity,
    billingManager: BillingManager,
    preferences: PreferencesRepository,
    settings: V19SettingsRepository,
    appLock: V19AppLock
) {
    var showHub by remember { mutableStateOf(false) }
    var unlocked by remember { mutableStateOf(!settings.appLockEnabled || !appLock.configured) }
    var unlockPin by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize()) {
        QrBarcodeApp(activity, billingManager, preferences)

        FloatingActionButton(
            onClick = { showHub = true },
            modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
        ) {
            Text("1.9", fontWeight = FontWeight.Black)
        }

        if (showHub) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                V19FeatureHub(
                    preferences = preferences,
                    settings = settings,
                    appLock = appLock,
                    onClose = { showHub = false }
                )
            }
        }
    }

    if (!unlocked) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("قفل QR یار") },
            text = {
                OutlinedTextField(
                    value = unlockPin,
                    onValueChange = { unlockPin = it.filter(Char::isDigit).take(8) },
                    label = { Text("PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (appLock.verify(unlockPin)) {
                        unlocked = true
                        unlockPin = ""
                    }
                }) { Text("باز کردن") }
            }
        )
    }
}

@Composable
private fun V19FeatureHub(
    preferences: PreferencesRepository,
    settings: V19SettingsRepository,
    appLock: V19AppLock,
    onClose: () -> Unit
) {
    var productName by remember { mutableStateOf("محصول نمونه") }
    var productPrice by remember { mutableStateOf("12000000") }
    var productCode by remember { mutableStateOf("123456789012") }
    var testUrl by remember { mutableStateOf("https://example.com") }
    var pin by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var preview by remember { mutableStateOf<Bitmap?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("مرکز امکانات نسخه 1.9", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("مجموع قابلیت‌های نسخه‌های پیشنهادی 1.2 تا 1.9")
                }
                TextButton(onClick = onClose) { Text("بستن") }
            }
        }

        item { FeatureCard("🎨 QR Designer", "قالب‌های آماده، طراحی حرفه‌ای، ذخیره الگو، Preview و مسیر توسعه رنگ/قاب/لوگو") }
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("قالب‌های هوشمند", fontWeight = FontWeight.Black)
                    SmartTemplateCatalog.items.forEach { template ->
                        Text("${template.emoji} ${template.title}${if (template.premium) "  👑" else ""} — ${template.hint}")
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🏷️ Barcode Studio / لیبل فروشگاهی", fontWeight = FontWeight.Black)
                    OutlinedTextField(productName, { productName = it.take(48) }, modifier = Modifier.fillMaxWidth(), label = { Text("نام محصول") })
                    OutlinedTextField(productPrice, { productPrice = it.filter(Char::isDigit).take(14) }, modifier = Modifier.fillMaxWidth(), label = { Text("قیمت") })
                    OutlinedTextField(productCode, { productCode = it.take(64) }, modifier = Modifier.fillMaxWidth(), label = { Text("کد کالا") })
                    Button(
                        onClick = {
                            preview?.recycle()
                            preview = runCatching {
                                ProductLabelRenderer.render(
                                    ProductLabelSpec(productName, productPrice.toLongOrNull() ?: 0L, productCode)
                                )
                            }.getOrNull()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("ساخت پیش‌نمایش لیبل") }
                    preview?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "پیش نمایش لیبل",
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔐 امنیت لینک", fontWeight = FontWeight.Black)
                    OutlinedTextField(testUrl, { testUrl = it }, modifier = Modifier.fillMaxWidth(), label = { Text("URL برای بررسی") })
                    val assessment = V19LinkSecurity.assess(testUrl)
                    Text("امتیاز ریسک: ${assessment.score}/100")
                    Text(if (assessment.reasons.isEmpty()) "نشانه مشکوک مشخصی پیدا نشد." else assessment.reasons.joinToString(" • "))
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📷 تنظیمات اسکنر حرفه‌ای", fontWeight = FontWeight.Black)
                    ToggleRow("صدای اسکن", settings.scannerBeep) { settings.scannerBeep = it }
                    ToggleRow("لرزش بعد از اسکن", settings.scannerVibrate) { settings.scannerVibrate = it }
                    ToggleRow("اسکن متوالی", settings.continuousScan) { settings.continuousScan = it }
                    ToggleRow("جلوگیری از نتیجه تکراری", settings.preventDuplicates) { settings.preventDuplicates = it }
                    ToggleRow("تأیید قبل از باز کردن لینک", settings.confirmBeforeOpeningLinks) { settings.confirmBeforeOpeningLinks = it }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🗂️ آرشیو، Folder و Tag", fontWeight = FontWeight.Black)
                    Text("مدل Folder/Tag برای اتصال به Room اضافه شده و تاریخچه فعلی همچنان Search/Filter/Favorite/Delete را حفظ می‌کند.")
                    Text("رکوردهای فعلی: ${preferences.history().size}")
                    Button(onClick = {
                        val json = V19BackupManager.buildJson(preferences.history(), settings)
                        val file = V19BackupManager.saveToCache((preferences.javaClass.getDeclaredField("appContext").apply { isAccessible = true }.get(preferences) as android.content.Context), json)
                        message = "بکاپ محلی ساخته شد: ${file.name}"
                    }) { Text("ساخت بکاپ JSON") }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✨ شخصی‌سازی", fontWeight = FontWeight.Black)
                    ToggleRow("حالت Compact", settings.compactMode) { settings.compactMode = it }
                    Text("Accent فعلی: ${settings.accentName}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { settings.accentName = "صورتی یاسی" }) { Text("صورتی") }
                        OutlinedButton(onClick = { settings.accentName = "سبز نعنایی" }) { Text("نعنایی") }
                        OutlinedButton(onClick = { settings.accentName = "آبی آسمانی" }) { Text("آبی") }
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔒 قفل برنامه با PIN", fontWeight = FontWeight.Black)
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it.filter(Char::isDigit).take(8) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("PIN چهار تا هشت رقمی") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            if (appLock.setPin(pin)) {
                                settings.appLockEnabled = true
                                message = "قفل PIN فعال شد."
                                pin = ""
                            } else message = "PIN باید 4 تا 8 رقم باشد."
                        }) { Text("فعال‌سازی") }
                        OutlinedButton(onClick = {
                            appLock.clear()
                            settings.appLockEnabled = false
                            message = "قفل برنامه غیرفعال شد."
                        }) { Text("غیرفعال") }
                    }
                }
            }
        }

        item { FeatureCard("📚 Batch & Print", "CSV/TXT/XLSX، PNG گروهی و PDF لیبل A4 نسخه قبل حفظ شده و برای Mapping ستونی/ZIP آماده توسعه شده است.") }
        item { FeatureCard("🛍️ ابزارهای فروشگاهی", "کارت محصول و لیبل فروشگاهی به موتور Barcode متصل شده؛ QR منو، Wi-Fi، شبکه اجتماعی و موقعیت نیز در قالب‌های هوشمند قرار دارند.") }

        if (message.isNotBlank()) {
            item {
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(message, Modifier.fillMaxWidth().padding(14.dp), textAlign = TextAlign.Center)
                }
            }
        }
        item { Spacer(Modifier.height(70.dp)) }
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun FeatureCard(title: String, body: String) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.Black)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
